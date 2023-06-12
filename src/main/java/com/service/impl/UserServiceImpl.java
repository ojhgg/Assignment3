package com.service.impl;
import com.auth0.jwt.algorithms.Algorithm;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mapper.*;
import com.pojo.*;
import com.pojo.Billing;
import com.pojo.Shipment;
import com.vo.R;
import com.vo.param.*;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.auth0.jwt.JWT;
import com.service.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;


/**
 * FileName:  UserServiceImpl
 * Date: 2023/04/13
 */
@Service
@AllArgsConstructor
public  class UserServiceImpl implements UserService {
    @Autowired
    private final ShipperAccountMapper userMapper;
    @Autowired
    private final ShipmentMapper shipmentMapper;
    @Autowired
    private final BillingMapper billingMapper;
    @Autowired
    private final ParcelMapper parcelMapper;

    // 登录
    @Override
    public R login(LoginParam loginParam) {
        QueryWrapper<Shipperaccount> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", loginParam.getUsername()).eq("password", loginParam.getPassword());
        R r = new R();
        List<Shipperaccount> shipper = userMapper.selectList(queryWrapper);//
        if (shipper != null) {
            String token = getToken(loginParam);
            r.data("token", token);
            r.data("status_code", true);
            return r;
        } else {
            r.data("status_code", false);
            r.data("token", "");
            return r;
        }
    }

    // 注册
    @Override
    public R register(RegisterParam registerParam) {
        R r = new R();
        r.data("status_code", false);
        QueryWrapper<Shipperaccount> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", registerParam.getId());
        boolean u = userMapper.exists(queryWrapper);
        System.out.println(u);
        if (!u) {
            Date now = new Date();
            Shipperaccount shipperAccount = new Shipperaccount();
            shipperAccount.setPassword(registerParam.getPassword());
            shipperAccount.setContactName(registerParam.getContactname());
            shipperAccount.setId(registerParam.getId());
            shipperAccount.setCreatedAt(LocalDate.now().toString());
            shipperAccount.setAddress(registerParam.getAddress());
            int i2 = userMapper.insert(shipperAccount);
            if (i2 == 1) {
                r.data("status_code", true);
                return r;
            }
            return r;
        } else {
            r.setMessage("用户已存在");
            return r;
        }
    }

    @Override
    public R Billing(String accountnumber) {
        R r = new R();
        r.data("status_code", false);
        QueryWrapper<Billing> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("accountnumber", accountnumber);
        try {
            Billing billing = billingMapper.selectOne(queryWrapper);
            r.data("billing", billing);
            r.data("status_code", true);
            return r;
        } catch (Exception e) {
            r.setMessage(e.toString());
        }
        return r;
    }
    //修改密码


    @Override
    public R tracking(String itemid) {
        R r = new R();
        r.data("status_code", false);
        QueryWrapper<Shipment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parcels", itemid);
        Shipment shipment = shipmentMapper.selectOne(queryWrapper);
        if (shipment != null) {

            if (shipment.getReturnTo() != null) {
                r.data("address", shipment.getReturnTo());
                r.data("status_code", true);
                return r;
            }
        }
        r.setMessage("记录不存在");
        return r;
    }

    public String getToken(LoginParam user) {
        String token;
        token = JWT.create().withAudience(user.getUsername())
                .sign(Algorithm.HMAC256(user.getPassword()));
        return token;
    }

    // 主页请求用户信息
    public Shipperaccount findUserById(String id) {
        Shipperaccount shipperaccount;
        QueryWrapper<Shipperaccount> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        shipperaccount = userMapper.selectOne(queryWrapper);
        return shipperaccount;

    }

    @Override
    public R createOrder(CreateOrderParam createOrderParam, String id) {
        R r = new R();
        int i=0, j=0, k=0;
        r.data("status_code", false);
        Billing billing = new Billing();
        Shipment shipment1 = new Shipment();
        Parcel parcels = new Parcel();
        Parcels p=createOrderParam.getParcels();
            shipment1.setParcels(p.getParcelsid());
            parcels.setParcelsid(p.getParcelsid());
            parcels.setDescription(p.getDescription());
            parcels.setWeight(p.getWeight());
            parcels.setQuantity(p.getQuantity());
            parcels.setBoxtype(parcels.getBoxtype());
            shipment1.setReturnTo(createOrderParam.getReturnTo());
            shipment1.setShipFrom(createOrderParam.getShipFrom());
            shipment1.setShipTo(createOrderParam.getShipTo());
            shipment1.setServicetype(createOrderParam.getType());
            billing.setAccountNumber(id);
            billing.setType(createOrderParam.getType());
            billing.setPaidBy(id);
            i = shipmentMapper.insert(shipment1);
            j = billingMapper.insert(billing);
            k = parcelMapper.insert(parcels);

        if (i == 1 && j == 1 && k == 1) r.data("status_code", true);
        return r;//创建订单
    }
}
