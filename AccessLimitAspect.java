package com.baec.antiviral.lib.limit;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description
 * @Author dingjy
 * @Date 2021/12/27 15:48
 */
@Slf4j
@Aspect
public class AccessLimitAspect {
    //失效时间
    private final static long EXPIRE_TIME=60*1000;
    //key表示ip+path  模拟ip访问限制   注意数据量不能太大
    private Map<String,AccessLimitCacheObject> map=new ConcurrentHashMap<>();

    @Pointcut("@annotation(com.baec.antiviral.lib.limit.AccessLimit)")
    public void pointcut(){}


    @Around("pointcut()&&@annotation(limit)")
    public void process(ProceedingJoinPoint process,AccessLimit limit){
        MethodSignature signature = (MethodSignature)process.getSignature();
        Method method = signature.getMethod();
        RequestMapping annotation = method.getAnnotation(RequestMapping.class);
        if(annotation==null){
            log.error("必须在RequestMapping注解下使用该注解");
            return;
        }


        String ip;
        try {
            ip= LocalHostUtil.getLocalIP();
        } catch (UnknownHostException e) {
            log.error("获取主机ip地址异常",e);
            ip="unKnownHost";
        }

        String path = annotation.value()[0];//路由路径,只取第一个
        int count = limit.value();//限定次数
        String key=ip+path;


        synchronized (key){
            AccessLimitCacheObject cache = map.get(key);
            if(cache==null){
                map.put(key,new AccessLimitCacheObject(1,EXPIRE_TIME));
            }else if(expireTime(0l)>cache.expireTime){
                cache.setCount(1);
                cache.setExpireTime(EXPIRE_TIME);
            }else if(cache.count<=count){
                cache.setCount(cache.count++);
            }else{
                log.error("当前用户IP:{} 访问路径:{}，超过限定次数:{}",ip,path,count);
                return;
            }
        }


        try{
           process.proceed();
        }catch (Throwable t) {
            log.error("AccessLimitAspect error",t);
        }
    }

    /**
     * 获取失效时间 当前时间+失效时间
     * @param expireTime
     * @return
     */
    private Long expireTime(Long expireTime) {
        Date date = new Date();
        return date.getTime()+expireTime;
    }


    private class AccessLimitCacheObject{
        //当前访问次数
        private int count;
        //超时时间
        private Long expireTime;

        public AccessLimitCacheObject(int count,Long expireTime){
            this.count=count;
            this.expireTime=expireTime;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public Long getExpireTime() {
            return expireTime;
        }

        public void setExpireTime(Long expireTime) {
            this.expireTime = expireTime;
        }
    }

}
