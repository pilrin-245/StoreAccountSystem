package com.store.dao;

import com.store.entity.StockRecord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 库存流水数据访问层。
 */
public class StockDao extends BaseDao {

    private static final String SELECT_JOIN =
            "SELECT sr.id, sr.goods_id, sr.change_num, sr.in_out_type, sr.source_type, sr.remark, "
                    + "sr.operate_time, sr.operator_id, g.name AS goods_name, g.unit AS goods_unit, "
                    + "u.name AS operator_name "
                    + "FROM t_stock_record sr "
                    + "LEFT JOIN t_goods g ON sr.goods_id = g.id "
                    + "LEFT JOIN t_user u ON sr.operator_id = u.id ";

    private StockRecord map(ResultSet rs) throws SQLException {
        StockRecord record = new StockRecord();
        record.setId(rs.getInt("id"));
        record.setGoodsId(rs.getInt("goods_id"));
        record.setChangeNum(rs.getInt("change_num"));
        record.setInOutType(rs.getString("in_out_type"));
        record.setSourceType(rs.getString("source_type"));
        record.setRemark(rs.getString("remark"));
        record.setOperateTime(rs.getTimestamp("operate_time"));
        record.setOperatorId(rs.getInt("operator_id"));
        record.setGoodsName(rs.getString("goods_name"));
        record.setGoodsUnit(rs.getString("goods_unit"));
        record.setOperatorName(rs.getString("operator_name"));
        return record;
    }

    public int insert(StockRecord record) {
        return insert("INSERT INTO t_stock_record (goods_id, change_num, in_out_type, source_type, remark, "
                        + "operator_id) VALUES (?,?,?,?,?,?)",
                record.getGoodsId(), record.getChangeNum(), record.getInOutType(), record.getSourceType(),
                record.getRemark(), record.getOperatorId());
    }

    public List<StockRecord> search(Integer goodsId, String inOutType, String keyword, Timestamp start,
                                    Timestamp end, int offset, int limit) {
        List<Object> args = new ArrayList<>();
        String sql = SELECT_JOIN + buildWhere(goodsId, inOutType, keyword, start, end, args)
                + " ORDER BY sr.operate_time DESC, sr.id DESC LIMIT ? OFFSET ?";
        args.add(limit);
        args.add(offset);
        return queryList(sql, this::map, args.toArray());
    }

    public int count(Integer goodsId, String inOutType, String keyword, Timestamp start, Timestamp end) {
        List<Object> args = new ArrayList<>();
        String sql = "SELECT COUNT(*) FROM t_stock_record sr LEFT JOIN t_goods g ON sr.goods_id = g.id "
                + buildWhere(goodsId, inOutType, keyword, start, end, args);
        return queryInt(sql, args.toArray());
    }

    private String buildWhere(Integer goodsId, String inOutType, String keyword, Timestamp start, Timestamp end,
                              List<Object> args) {
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        if (goodsId != null) {
            where.append(" AND sr.goods_id = ?");
            args.add(goodsId);
        }
        if (inOutType != null && !inOutType.isEmpty()) {
            where.append(" AND sr.in_out_type = ?");
            args.add(inOutType);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            where.append(" AND (g.name LIKE ? OR sr.remark LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
        }
        if (start != null) {
            where.append(" AND sr.operate_time >= ?");
            args.add(start);
        }
        if (end != null) {
            where.append(" AND sr.operate_time <= ?");
            args.add(end);
        }
        return where.toString();
    }

    public List<StockRecord> recent(int limit) {
        return queryList(SELECT_JOIN + "ORDER BY sr.operate_time DESC, sr.id DESC LIMIT ?", this::map, limit);
    }
}
