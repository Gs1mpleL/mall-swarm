package com.macro.mall.portal.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.BooleanUtil;
import com.github.pagehelper.PageHelper;
import com.macro.mall.common.exception.ApiException;
import com.macro.mall.common.service.RedisService;
import com.macro.mall.mapper.*;
import com.macro.mall.model.*;
import com.macro.mall.portal.dao.HomeDao;
import com.macro.mall.portal.domain.FlashPromotionProduct;
import com.macro.mall.portal.domain.HomeContentResult;
import com.macro.mall.portal.domain.HomeFlashPromotion;
import com.macro.mall.portal.domain.SignDayRes;
import com.macro.mall.portal.service.HomeService;
import com.macro.mall.portal.service.UmsMemberService;
import com.macro.mall.portal.util.DateUtil;
import com.macro.mall.portal.util.UserUtils;
import org.apache.http.client.utils.DateUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 首页内容管理Service实现类
 * Created by macro on 2019/1/28.
 */
@Service
public class HomeServiceImpl implements HomeService {
    @Autowired
    private SmsHomeAdvertiseMapper advertiseMapper;
    @Autowired
    private HomeDao homeDao;
    @Autowired
    private SmsFlashPromotionMapper flashPromotionMapper;
    @Autowired
    private SmsFlashPromotionSessionMapper promotionSessionMapper;
    @Autowired
    private PmsProductMapper productMapper;
    @Autowired
    private PmsProductCategoryMapper productCategoryMapper;
    @Autowired
    private CmsSubjectMapper subjectMapper;
    @Autowired
    private UmsMemberService memberService;
    @Autowired
    private RedisService redisService;

    @Override
    public HomeContentResult content() {
        HomeContentResult result = new HomeContentResult();
        //获取首页广告
        result.setAdvertiseList(getHomeAdvertiseList());
        //获取推荐品牌
        result.setBrandList(homeDao.getRecommendBrandList(0,6));
        //获取秒杀信息
        result.setHomeFlashPromotion(getHomeFlashPromotion());
        //获取新品推荐
        result.setNewProductList(homeDao.getNewProductList(0,4));
        //获取人气推荐
        result.setHotProductList(homeDao.getHotProductList(0,4));
        //获取推荐专题
        result.setSubjectList(homeDao.getRecommendSubjectList(0,4));
        return result;
    }

    @Override
    public List<PmsProduct> recommendProductList(Integer pageSize, Integer pageNum) {
        // TODO: 2019/1/29 暂时默认推荐所有商品
        PageHelper.startPage(pageNum,pageSize);
        PmsProductExample example = new PmsProductExample();
        example.createCriteria()
                .andDeleteStatusEqualTo(0)
                .andPublishStatusEqualTo(1);
        return productMapper.selectByExample(example);
    }

    @Override
    public List<PmsProductCategory> getProductCateList(Long parentId) {
        PmsProductCategoryExample example = new PmsProductCategoryExample();
        example.createCriteria()
                .andShowStatusEqualTo(1)
                .andParentIdEqualTo(parentId);
        example.setOrderByClause("sort desc");
        return productCategoryMapper.selectByExample(example);
    }

    @Override
    public List<CmsSubject> getSubjectList(Long cateId, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum,pageSize);
        CmsSubjectExample example = new CmsSubjectExample();
        CmsSubjectExample.Criteria criteria = example.createCriteria();
        criteria.andShowStatusEqualTo(1);
        if(cateId!=null){
            criteria.andCategoryIdEqualTo(cateId);
        }
        return subjectMapper.selectByExample(example);
    }

    @Override
    public List<PmsProduct> hotProductList(Integer pageNum, Integer pageSize) {
        int offset = pageSize * (pageNum - 1);
        return homeDao.getHotProductList(offset, pageSize);
    }

    @Override
    public List<PmsProduct> newProductList(Integer pageNum, Integer pageSize) {
        int offset = pageSize * (pageNum - 1);
        return homeDao.getNewProductList(offset, pageSize);
    }

    private HomeFlashPromotion getHomeFlashPromotion() {
        HomeFlashPromotion homeFlashPromotion = new HomeFlashPromotion();
        //获取当前秒杀活动
        Date now = new Date();
        SmsFlashPromotion flashPromotion = getFlashPromotion(now);
        if (flashPromotion != null) {
            //获取当前秒杀场次
            SmsFlashPromotionSession flashPromotionSession = getFlashPromotionSession(now);
            if (flashPromotionSession != null) {
                homeFlashPromotion.setStartTime(flashPromotionSession.getStartTime());
                homeFlashPromotion.setEndTime(flashPromotionSession.getEndTime());
                //获取下一个秒杀场次
                SmsFlashPromotionSession nextSession = getNextFlashPromotionSession(homeFlashPromotion.getStartTime());
                if(nextSession!=null){
                    homeFlashPromotion.setNextStartTime(nextSession.getStartTime());
                    homeFlashPromotion.setNextEndTime(nextSession.getEndTime());
                }
                //获取秒杀商品
                List<FlashPromotionProduct> flashProductList = homeDao.getFlashProductList(flashPromotion.getId(), flashPromotionSession.getId());
                homeFlashPromotion.setProductList(flashProductList);
            }
        }
        return homeFlashPromotion;
    }

    //获取下一个场次信息

    private SmsFlashPromotionSession getNextFlashPromotionSession(Date date) {
        SmsFlashPromotionSessionExample sessionExample = new SmsFlashPromotionSessionExample();
        sessionExample.createCriteria()
                .andStartTimeGreaterThan(date);
        sessionExample.setOrderByClause("start_time asc");
        List<SmsFlashPromotionSession> promotionSessionList = promotionSessionMapper.selectByExample(sessionExample);
        if (!CollectionUtils.isEmpty(promotionSessionList)) {
            return promotionSessionList.get(0);
        }
        return null;
    }
    private List<SmsHomeAdvertise> getHomeAdvertiseList() {
        SmsHomeAdvertiseExample example = new SmsHomeAdvertiseExample();
        example.createCriteria().andTypeEqualTo(1).andStatusEqualTo(1);
        example.setOrderByClause("sort desc");
        return advertiseMapper.selectByExample(example);
    }

    //根据时间获取秒杀活动

    private SmsFlashPromotion getFlashPromotion(Date date) {
        Date currDate = DateUtil.getDate(date);
        SmsFlashPromotionExample example = new SmsFlashPromotionExample();
        example.createCriteria()
                .andStatusEqualTo(1)
                .andStartDateLessThanOrEqualTo(currDate)
                .andEndDateGreaterThanOrEqualTo(currDate);
        List<SmsFlashPromotion> flashPromotionList = flashPromotionMapper.selectByExample(example);
        if (!CollectionUtils.isEmpty(flashPromotionList)) {
            return flashPromotionList.get(0);
        }
        return null;
    }
    //根据时间获取秒杀场次

    private SmsFlashPromotionSession getFlashPromotionSession(Date date) {
        Date currTime = DateUtil.getTime(date);
        SmsFlashPromotionSessionExample sessionExample = new SmsFlashPromotionSessionExample();
        sessionExample.createCriteria()
                .andStartTimeLessThanOrEqualTo(currTime)
                .andEndTimeGreaterThanOrEqualTo(currTime);
        List<SmsFlashPromotionSession> promotionSessionList = promotionSessionMapper.selectByExample(sessionExample);
        if (!CollectionUtils.isEmpty(promotionSessionList)) {
            return promotionSessionList.get(0);
        }
        return null;
    }

    @Override
    public SignDayRes signDay() {
        UmsMember member = UserUtils.getUserDetail();
        String key = "mall:portal:signDay:%s:%s";
        String finalKey = String.format(key, member.getId(), DateUtils.formatDate(new Date(), "yyyy-MM"));
        int day = DateTime.now().dayOfMonth();
        if (redisService.bitGet(finalKey, day)) {
            return mapSignDayRes(finalKey,day,"已经签到过了");
        }
        Boolean oldValue = redisService.bitSet(finalKey, day, true);
        // 上述方法返回是该位置的旧value
        if (BooleanUtil.isTrue(oldValue)) {
            throw new ApiException("签到失败");
        }
        return mapSignDayRes(finalKey, day,"签到成功");
    }

    /**
     * 封装签到返回信息
     */
    @NotNull
    private SignDayRes mapSignDayRes(String finalKey, int day,String msg) {
        SignDayRes signDayRes = new SignDayRes();
        // 返回msg
        signDayRes.setMsg(msg);
        // 签到计数
        signDayRes.setAllCount(Math.toIntExact(redisService.bitCount(finalKey)));
        // 签到位图
        String bitStr = mapBitMapAllMonth(finalKey, day, signDayRes);
        // 连续签到次数
        int tmpCount = 0;
        int maxCount = 0;
        byte[] bytes = bitStr.getBytes();
        for (int i = bitStr.length() - 1; i >= 0; i--) {
            if (bytes[i] == '1'){
                tmpCount++;
                maxCount = Math.max(maxCount,tmpCount);
            }else{
                maxCount = Math.max(maxCount,tmpCount);
                tmpCount = 0;
            }
        }
        signDayRes.setMaxContinuityCount(maxCount);
        return signDayRes;
    }

    @NotNull
    private String mapBitMapAllMonth(String finalKey, int day, SignDayRes signDayRes) {
        // 获取日历对象
        Calendar c = Calendar.getInstance();
        // 设置日历对象为指定年月日，为指定月份的第一天
        c.set(LocalDate.now().getYear(), LocalDate.now().getMonthValue(), 1);
        // 设置日历对象，指定月份往前推一天，也就是最后一天
        c.add(Calendar.DATE, -1);
        // 获取本月有多少天
        int allDay = c.get(Calendar.DATE);
        String bitStr = redisService.getBitStr(finalKey, day);
        int bitLength = bitStr.length();
        int zeroFill = day - bitLength;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < zeroFill; i++) {
            stringBuilder.append("0");
        }
        stringBuilder.append(bitStr);
        for (int i = 0; i < allDay- day; i++) {
            stringBuilder.append("0");
        }
        signDayRes.setCountMap(stringBuilder.toString());
        return bitStr;
    }
}
