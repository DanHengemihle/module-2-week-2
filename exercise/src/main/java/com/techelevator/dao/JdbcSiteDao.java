package com.techelevator.dao;

import com.techelevator.model.Site;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JdbcSiteDao implements SiteDao {

    private JdbcTemplate jdbcTemplate;

    public JdbcSiteDao(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public List<Site> getSitesThatAllowRVs(int parkId) {
        List<Site> sites = new ArrayList<>();
        String sql = "SELECT * FROM site JOIN campground ON site.campground_id = campground.campground_id WHERE park_id = ? AND max_rv_length > 0";
        SqlRowSet result = jdbcTemplate.queryForRowSet(sql, parkId);
        while(result.next()){
            sites.add(mapRowToSite(result));
        }
        return sites;
    }

    @Override
    public List<Site> getAllAvailableSitesInParks(int parkId) {
        List<Site> sites = new ArrayList<>();
        String sql = "SELECT site.site_id, site.campground_id, site_number, max_occupancy, accessible, max_rv_length, utilities FROM site JOIN campground ON site.campground_id = campground.campground_id JOIN park ON campground.park_id = park.park_id JOIN reservation ON site.site_id = reservation.site_id WHERE from_date > CURRENT_DATE AND park.park_id = ?";
        SqlRowSet result = jdbcTemplate.queryForRowSet(sql, parkId);
        while(result.next()){
            sites.add(mapRowToSite(result));
        }
        return sites;
    }

    @Override
    public List<Site> getAvailableSitesWithinDateRange(int parkId, LocalDate fromDate, LocalDate toDate) {
        List<Site> sites = new ArrayList<>();
        String sql = "SELECT site.site_id, site.campground_id, site_number, max_occupancy, accessible, max_rv_length, utilities FROM site JOIN reservation ON site.site_id = reservation.site_id JOIN campground ON site.campground_id = campground.campground_id JOIN park ON campground.park_id = park.park_id WHERE park.park_id = ? AND site.site_id NOT IN(SELECT reservation.site_id FROM reservation WHERE CURRENT_DATE > ? AND CURRENT_DATE + 1 < ?)";
        SqlRowSet result = jdbcTemplate.queryForRowSet(sql, parkId, fromDate, toDate);
        while(result.next()){
            sites.add(mapRowToSite(result));
        }
        return sites;
    }

    private Site mapRowToSite(SqlRowSet results) {
        Site site = new Site();
        site.setSiteId(results.getInt("site_id"));
        site.setCampgroundId(results.getInt("campground_id"));
        site.setSiteNumber(results.getInt("site_number"));
        site.setMaxOccupancy(results.getInt("max_occupancy"));
        site.setAccessible(results.getBoolean("accessible"));
        site.setMaxRvLength(results.getInt("max_rv_length"));
        site.setUtilities(results.getBoolean("utilities"));
        return site;
    }
}
