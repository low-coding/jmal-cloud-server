package com.jmal.clouddisk.util;

import cn.hutool.core.util.URLUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.jmal.clouddisk.model.rbac.ConsumerDO;
import com.jmal.clouddisk.oss.BucketInfo;
import com.jmal.clouddisk.webdav.MyWebdavServlet;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * CaffeineUtil
 *
 * @author jmal
 */
@Component
public class CaffeineUtil {

    /**
     * 缩略图请求缓存
     */
    private static final Cache<String, Boolean> THUMBNAIL_REQUEST_CACHE = Caffeine.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).build();

    /**
     * 用户oss存储路径前缀缓存
     * key: 路径前缀，例如：/jmal/aliyunStorage ,其中jmal为用户名,aliyunStorage 为oss存储的挂载文件夹名称，由用户自定义
     * value: BucketInfo
     */
    private static final Cache<String, BucketInfo> OSS_DIAMETER_PREFIX_CACHE = Caffeine.newBuilder().build();

    private static final Cache<String, Long> LAST_ACCESS_TIME_CACHE = Caffeine.newBuilder().build();

    /***
     * 空间已满的用户
     */
    private static final Cache<String, String> SPACE_FULL = Caffeine.newBuilder().build();

    /***
     * 用户信息缓存
     * key: username
     * value: ConsumerDO 用户信息
     */
    public static final Cache<String, ConsumerDO> CONSUMER_USERNAME = Caffeine.newBuilder().build();

    /***
     * 已上传的分片索引
     */
    private static Cache<String, CopyOnWriteArrayList<Integer>> resumeCache;

    /***
     * 已写入的分片索引
     */
    private static Cache<String, CopyOnWriteArrayList<Integer>> writtenCache;

    /***
     * 未写入(已上传)的分片索引
     */
    private static Cache<String, CopyOnWriteArrayList<Integer>> unWrittenCache;

    /***
     * 分片写锁
     */
    private static Cache<String, Lock> chunkWriteLockCache;

    /***
     * 上传文件夹锁
     */
    private static Cache<String, Lock> uploadFolderLockCache;

    /***
     * 用户身份权限缓存
     * key: username
     * value: 权限标识列表
     */
    private static final Cache<String, List<String>> AUTHORITIES_CACHE = Caffeine.newBuilder().build();

    /***
     * 缓存userId
     * key: username
     * value: userId
     */
    private static final Cache<String, String> USER_ID_CACHE = Caffeine.newBuilder().build();

    public static List<String> getAuthoritiesCache(String username) {
        return AUTHORITIES_CACHE.getIfPresent(username);
    }

    /***
     * 缓存中是否存在该username的权限
     * @param username username
     * @return bool
     */
    public static boolean existsAuthoritiesCache(String username) {
        return AUTHORITIES_CACHE.getIfPresent(username) != null;
    }

    public static void setAuthoritiesCache(String username, List<String> authorities) {
        AUTHORITIES_CACHE.put(username, authorities);
    }

    public static void removeAuthoritiesCache(String username) {
        AUTHORITIES_CACHE.invalidate(username);
    }

    public static String getUserIdCache(String username) {
        return USER_ID_CACHE.getIfPresent(username);
    }

    public static void setUserIdCache(String username, String userId) {
        USER_ID_CACHE.put(username, userId);
    }

    public static void removeUserIdCache(String username) {
        USER_ID_CACHE.invalidate(username);
    }

    @PostConstruct
    public void initCache(){
        initMyCache();
    }

    public static void initMyCache(){
        if(resumeCache == null){
            resumeCache = Caffeine.newBuilder().build();
        }
        if(writtenCache == null){
            writtenCache = Caffeine.newBuilder().build();
        }
        if(unWrittenCache == null){
            unWrittenCache = Caffeine.newBuilder().build();
        }
        if(chunkWriteLockCache == null){
            chunkWriteLockCache = Caffeine.newBuilder().build();
        }
        if(uploadFolderLockCache == null) {
            uploadFolderLockCache = Caffeine.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).build();
        }
    }

    public static Cache<String, CopyOnWriteArrayList<Integer>> getResumeCache(){
        if(resumeCache == null){
            initMyCache();
        }
        return resumeCache;
    }

    public static Cache<String, CopyOnWriteArrayList<Integer>> getWrittenCache(){
        if(writtenCache == null){
            initMyCache();
        }
        return writtenCache;
    }

    public static Cache<String, CopyOnWriteArrayList<Integer>> getUnWrittenCacheCache(){
        if(unWrittenCache == null){
            initMyCache();
        }
        return unWrittenCache;
    }

    public static Cache<String, Lock> getChunkWriteLockCache(){
        if(chunkWriteLockCache == null){
            initMyCache();
        }
        return chunkWriteLockCache;
    }

    public static Cache<String, Lock> getUploadFolderLockCache(){
        if(uploadFolderLockCache == null){
            initMyCache();
        }
        return uploadFolderLockCache;
    }

    public static void setSpaceFull(String userId) {
        SPACE_FULL.put(userId, userId);
    }

    public static void removeSpaceFull(String userId) {
        SPACE_FULL.invalidate(userId);
    }

    public static boolean spaceFull(String userId) {
        return SPACE_FULL.getIfPresent(userId) != null;
    }

    /**
     * 根据用户名获取用户信息
     */
    public static ConsumerDO getConsumerByUsernameCache(String username) {
        return CONSUMER_USERNAME.getIfPresent(username);
    }

    /**
     * 更新用户信息
     */
    public static void setConsumerByUsernameCache(String username, ConsumerDO consumerDO) {
        CONSUMER_USERNAME.put(username, consumerDO);
    }

    /**
     * 删除用户信息
     */
    public static void removeConsumerListByUsernameCache(List<ConsumerDO> list) {
        list.forEach(consumerDO -> removeConsumerByUsernameCache(consumerDO.getUsername()));
    }

    /**
     * 删除用户信息
     */
    public static void removeConsumerByUsernameCache(String username) {
        CONSUMER_USERNAME.invalidate(username);
    }

    public static Long getLastAccessTimeCache() {
        return LAST_ACCESS_TIME_CACHE.get("lastAccessTime", key -> System.currentTimeMillis());
    }

    public static void setLastAccessTimeCache() {
        LAST_ACCESS_TIME_CACHE.put("lastAccessTime", System.currentTimeMillis());
    }

    public static void setOssDiameterPrefixCache(String path, BucketInfo bucketInfo) {
        OSS_DIAMETER_PREFIX_CACHE.put(path, bucketInfo);
    }

    public static BucketInfo getOssDiameterPrefixCache(String path) {
        return OSS_DIAMETER_PREFIX_CACHE.getIfPresent(path);
    }

    public static void removeOssDiameterPrefixCache(String path) {
        OSS_DIAMETER_PREFIX_CACHE.invalidate(path);
    }

    /**
     * 获取oss path
     * @param path url path
     * @return oss path
     */
    public static String getOssPath(Path path) {
        path = Paths.get(URLUtil.decode(path.toString()));
        String prePath;
        if (path.getNameCount() >= 2) {
            prePath = MyWebdavServlet.PATH_DELIMITER + path.subpath(0, 2);
        } else {
            return null;
        }
        for (String prefixPath : OSS_DIAMETER_PREFIX_CACHE.asMap().keySet()) {
            if (prePath.equals(prefixPath)){
                return prefixPath;
            }
        }
        return null;
    }

    public static Boolean hasThumbnailRequestCache(String id) {
        return THUMBNAIL_REQUEST_CACHE.get(id, key -> false);
    }

    public static void setThumbnailRequestCache(String id) {
        THUMBNAIL_REQUEST_CACHE.put(id, true);
    }
}
