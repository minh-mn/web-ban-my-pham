package com.webshop.app.dao;

import com.webshop.app.model.FlashSale;
import com.webshop.app.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FlashSaleDAO extends DBConnection {
    public FlashSale findActiveFlashSale() {
        String sql = "SELECT * FROM flash_sales WHERE active = 1 AND NOW() BETWEEN start_time AND end_time LIMIT 1";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public List<FlashSale> findAll() {

        List<FlashSale> list = new ArrayList<>();

        String sql = """
            SELECT *
            FROM flash_sales
            ORDER BY id DESC
        """;

        try {

            PreparedStatement ps =
                    getConnection().prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            while(rs.next()){

                FlashSale f = map(rs);

                list.add(f);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public FlashSale findById(int id){

        String sql = """
            SELECT *
            FROM flash_sales
            WHERE id = ?
        """;

        try {

            PreparedStatement ps =
                    getConnection().prepareStatement(sql);

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if(rs.next()){
                return map(rs);
            }

        } catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public void insert(FlashSale f){

        String sql = """
            INSERT INTO flash_sales(
                title,
                start_time,
                end_time,
                active
            )
            VALUES(?,?,?,?)
        """;

        try {

            PreparedStatement ps =
                    getConnection().prepareStatement(sql);

            ps.setString(1, f.getTitle());

            ps.setTimestamp(2, f.getStartTime());

            ps.setTimestamp(3, f.getEndTime());

            ps.setBoolean(4, f.isActive());

            ps.executeUpdate();

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void update(FlashSale f){

        String sql = """
            UPDATE flash_sales
            SET
                title = ?,
                start_time = ?,
                end_time = ?,
                active = ?
            WHERE id = ?
        """;

        try {

            PreparedStatement ps =
                    getConnection().prepareStatement(sql);

            ps.setString(1, f.getTitle());

            ps.setTimestamp(2, f.getStartTime());

            ps.setTimestamp(3, f.getEndTime());

            ps.setBoolean(4, f.isActive());

            ps.setInt(5, f.getId());

            ps.executeUpdate();

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void delete(int id){

        String sql =
                "DELETE FROM flash_sales WHERE id=?";

        try {

            PreparedStatement ps =
                    getConnection().prepareStatement(sql);

            ps.setInt(1, id);

            ps.executeUpdate();

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private FlashSale map(ResultSet rs)
            throws SQLException {

        FlashSale f = new FlashSale();

        f.setId(rs.getInt("id"));

        f.setTitle(rs.getString("title"));

        f.setStartTime(
                rs.getTimestamp("start_time")
        );

        f.setEndTime(
                rs.getTimestamp("end_time")
        );

        f.setActive(
                rs.getBoolean("active")
        );

        return f;
    }

}