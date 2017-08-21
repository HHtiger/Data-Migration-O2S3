package com.tiger.util;

import com.huawei.obs.services.ObsClient;
import com.huawei.obs.services.exception.ObsException;
import com.huawei.obs.services.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Package: com.tiger.util
 * ClassName: OBSUtil
 * Author: Tiger
 * Description:
 * CreateDate: 2016/10/11
 * Version: 1.0
 */
public class OBSUtil {

    private static Logger logger = LoggerFactory.getLogger("upload");

    /**
     * 创建桶
     */
    public static void createBucket(ObsClient obsClient, String bucketName, String location) {
        try {
            // 调用create接口创建桶，并获得创建的桶对象
            S3Bucket rS3Bucket = obsClient.createBucket(bucketName, location);
            logger.debug("Bucket name: {}, location: {}", rS3Bucket.getBucketName(), rS3Bucket.getLocation());
        } catch (ObsException e) {
            logger.error("Create bucket failed. Error message: {} ResponseCode: {}", e.getErrorMessage(), e.getResponseCode());
        }
    }

    /**
     * 获取桶列表
     */
    public static void listBucket(ObsClient obsClient) {
        try {
            // 调用ObsService的listBuckets接口
            List<S3Bucket> bucketList = obsClient.listBuckets();
            Iterator<S3Bucket> iterator = bucketList.iterator();
            logger.debug("List bucket success : total size: {} ", bucketList.size());
            while (iterator.hasNext()) {
                logger.debug(" --------------------------------------");
                S3Bucket bucket = iterator.next();
                logger.debug(" BucketName: {}", bucket.getBucketName());
                logger.debug(" CreateDate: {}", bucket.getCreationDate());
                Owner owner = bucket.getOwner();
                if (null != owner) {
                    logger.debug(" Owner id: {}", owner.getId());
                    logger.debug(" Owner displayname: {}", owner.getDisplayName());
                } else {
                    logger.debug(" Owner is null");
                }
                logger.debug(" --------------------------------------");
            }
        } catch (ObsException e) {
            logger.error("Create object failed. Error message: {}. ResponseCode: {}. {}",
                    e.getErrorMessage(),
                    e.getResponseCode(),
                    e.getMessage());
        }
    }

    /**
     * 修改桶的ACL
     */
    private static void setBucketAcl(ObsClient obsClient, Owner own) {
        try {
            // 封装修改访问权限的请求
            AccessControlList acl = new AccessControlList();
            acl.setOwner(own);

            // 设置被授权用户的信息以及被赋予的权限
            CanonicalGrantee canonicalGrant1 = new CanonicalGrantee("20292A28C391D523CFC7406BDAAB7C15");
            canonicalGrant1.setDisplayName("esdk.25");
            CanonicalGrantee canonicalGrant2 = new CanonicalGrantee("20292A28C391D523CFC7406BDAAB7C15");
            canonicalGrant2.setDisplayName("esdk.26");
            acl.grantPermission(canonicalGrant1, Permission.PERMISSION_FULL_CONTROL);
            acl.grantPermission(canonicalGrant2, Permission.PERMISSION_READ);

            // 设置被授权用户组的信息以及被赋予的权限
            GroupGrantee groupGrant = new GroupGrantee("http://acs.amazonaws.com/groups/global/AuthenticatedUsers");
            acl.grantPermission(groupGrant, Permission.PERMISSION_READ);

            // 调用setBucketAcl设置桶的访问权限,
            // 这里cannedACL参数设置了为了null，该参数不能和acl同时使用
            // obsClient.setBucketAcl("image-bucket", null, acl);
            obsClient.setBucketAcl("123456-11", "public-read-write", null);
        } catch (ObsException e) {
            System.out.println("Error message: " + e.getErrorMessage() + " ResponseCode: " + e.getResponseCode());
        }
    }

    /**
     * 获取桶acl
     */
    public static void getBucketAcl(ObsClient obsClient, String bucketName) {
        try {
            AccessControlList accessControlList = obsClient.getBucketAcl(bucketName);
            GrantAndPermission[] grantAndPermissions = accessControlList.getGrantAndPermissions();
            Owner owner = accessControlList.getOwner();
            logger.debug("The owner of image-bucket: {}", owner.getDisplayName());
            for (GrantAndPermission grantAndPermission : grantAndPermissions) {
                logger.debug("GrantAndPermissions: {}", grantAndPermission);
            }
        } catch (ObsException e) {
            logger.error("failed to get bucket acl. Error message: {}.", e.getErrorMessage());
        }
    }

    /**
     * 删除桶
     */
    public static void deleteBucket(ObsClient obsClient, String bucketName) {
        try {
            obsClient.deleteBucket(bucketName);
            logger.debug("Delete bucket success.");
        } catch (ObsException e) {
            logger.error("Failed to delete bucket. Error message:{}.", e.getErrorMessage());
        }
    }

    /**
     * 获取桶的Policy
     */
    public static void getBucketPolicy(ObsClient obs, String bucketString) {
        try {
            obs.getBucketPolicy(bucketString);
            logger.debug("get bucket policy success");
        } catch (ObsException e) {
            logger.error("Failed to get bucket policy. Error message : {}.", e.getErrorMessage());
        }
    }

    /**
     * 获取桶内对象列表
     *
     * @param obsClient
     */
    public static void listObjects(ObsClient obsClient, ListObjectsRequest listObjectsRequest) {
        try {

            // 设置查询条件
//            ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
//            listObjectsRequest.setBucketName("123456-11");
//            listObjectsRequest.setPrefix("a");// 获得以wo为前缀的对象
//            listObjectsRequest.setMaxKeys(3);// 返回对象的最大数量

            // 调用listObjects接口查询image-bucket桶下的所有对象
            ObjectListing objListing = obsClient.listObjects(listObjectsRequest);

            // 输出桶内对象的信息
            List<S3Object> objList = objListing.getObjectSummaries();
            for (S3Object s3Object : objList) {
                logger.debug("ObjectKey: {}, Metadata: {}.", s3Object.getObjectKey(), s3Object.getMetadata().getMetadata());
            }
        } catch (ObsException e) {
            logger.error("List objects failed. Error message: {} . ResponseCode: {}", e.getErrorMessage(), e.getResponseCode());
        }
    }

    /**
     * 获取对象内容
     */
    public static void getObject(ObsClient obsClient, String bucketName, String objectKey, String versionId, Path localSavePath) {
        InputStream is = null;
        FileOutputStream fos = null;
        BufferedInputStream bif = null;
        BufferedOutputStream bof = null;
        try {
            S3Object s3 = obsClient.getObject(bucketName, objectKey, versionId);

            // 获得对象内容，并保存到本地
            is = s3.getObjectContent();
            File file = localSavePath.toFile();
            fos = new FileOutputStream(file);
            bif = new BufferedInputStream(is);
            bof = new BufferedOutputStream(fos);
            int b = -1;
            while ((b = bif.read()) != -1) {
                bof.write(b);
            }
            // 输出文件信息
            logger.debug("ObjectKey: {} , Metadata: {}", s3.getObjectKey(), s3.getMetadata().getMetadata());
        } catch (ObsException e) {
            logger.error("Get object failed. Error message: {}. ResponseCode: {}", e.getErrorMessage(), e.getResponseCode());
        } catch (FileNotFoundException e) {
            logger.error("FileNotFoundException: {}", e.getMessage());
        } catch (IOException e) {
            logger.error("IOException: {}", e.getMessage());
        } finally {
            try {
                if (null != bif)
                    bif.close();
                if (null != bof)
                    bof.close();
            } catch (IOException e) {
                logger.error("IOException: {}", e.getMessage());
            }
        }
    }

    /**
     * 查询对象的ACL
     */
    public static void getObjectAcl(ObsClient obsClient, String bucketName, String objectKey) {

        try {
            // 调用ObsService的getObjectAcl接口
            AccessControlList acl = obsClient.getObjectAcl(bucketName, objectKey, null);

            // 在控制台输出获取的对象ACL信息
            logger.debug("Get object acl success. Object: {}", objectKey);
            Owner owner = acl.getOwner();
            if (null != owner) {
                logger.debug(" Owner id: {}", owner.getId());
                logger.debug(" Owner displayname:{}", owner.getDisplayName());
            } else {
                logger.debug(" Owner is null");
            }
            GrantAndPermission[] grantAndPermissions = acl.getGrantAndPermissions();
            if (null != grantAndPermissions) {
                for (int i = 0; i < grantAndPermissions.length; i++) {
                    logger.debug(" Identifier : {}, Permissions : {}", grantAndPermissions[i].getGrantee().getIdentifier(), grantAndPermissions[i].getPermission());
                }
            }
        } catch (ObsException e) {
            logger.error("Get object acl failed. ErrorMessage: {}, Response code : {} ", e.getErrorMessage(), e.getResponseCode());
        }
    }

    /**
     * 修改对象的ACL
     */
    public static void setObjectAcl(ObsClient obsClient, String bucketName, String objectKey, Owner own, Permission permission) {
        // 封装修改访问权限的请求
        AccessControlList acl = new AccessControlList();
        acl.setOwner(own);

        // 设置被授权用户的信息以及被赋予的权限
        CanonicalGrantee canonicalGrant1 = new CanonicalGrantee(own.getId());
        canonicalGrant1.setDisplayName(own.getDisplayName());
        acl.grantPermission(canonicalGrant1, permission);

        // 调用setBucketAcl设置桶的访问权限
        try {
            obsClient.setObjectAcl(bucketName, objectKey, null, acl, null);
        } catch (ObsException e) {
            logger.error("Error message: {}, ResponseCode:{}, ResponseStatus:{}", e.getErrorMessage(), e.getResponseCode(), e.getErrorCode());
        }
    }

    /**
     * 获得对象元数据
     */
    public static void getObjectMetadata(ObsClient obsClient, String bucketName, String objectKey, String versionId) {
        try {
            ObjectMetadata metadata = obsClient.getObjectMetadata(bucketName, objectKey, versionId);
            logger.debug("ContentLength: {}, ContentType: {}, Metadata: {}", metadata.getContentLength(), metadata.getContentType(), metadata.getMetadata());
        } catch (ObsException e) {
            logger.error("Get metadata failed. Error message: {}, ResponseCode: {}, ResponseStatus: {}.", e.getErrorMessage(), e.getResponseCode(), e.getResponseStatus());
        }
    }

    /**
     * 删除对象
     */
    public static void deleteObject(ObsClient obsClient, String bucketName, String objectKey) {
        try {
            // 调用ObsService的deleteObject接口
            obsClient.deleteObject(bucketName, objectKey, null);
            logger.debug("Delete object success.");
        } catch (ObsException e) {
            logger.error("Delete object failed. {}, response code : {}.", e.getErrorMessage(), e.getResponseCode());
        }
    }

    /**
     * 初始化多段上传任务
     */
    public static void initMultipartUpload1(ObsClient obsClient, String bucketName, String objectKey) {
        try {

            // 封装初始化上传任务的请求
            InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest();
            request.setBucketName(bucketName);
            request.setObjectKey(objectKey);

            // 调用initiateMultipartUpload接口初始化上传任务
            InitiateMultipartUploadResult imu = obsClient.initiateMultipartUpload(request);
            logger.debug("BUCKE_TNAME_PREFIX:{} \tObjectKey: {} \tUploadId: {}", imu.getBucketName(), imu.getObjectKey(), imu.getUploadId());
        } catch (ObsException e) {
            logger.error("Initiate multipart upload failed. Error message: {}. ResponseCode: {}", e.getErrorMessage(), e.getResponseCode());
        }
    }

    /**
     * 上传段
     *
     * @param obsClient
     */
    public static void uploadMultipart1(ObsClient obsClient, String bucketName, String objectKey, Path uploadFilePath, String uploadId, int partNumber) {
        try {
            // 封装上传段的请求信息
            File file = uploadFilePath.toFile();// 要上传的文件
            UploadPartRequest request = new UploadPartRequest(bucketName, objectKey, file);
            request.setPartNumber(partNumber);
            request.setUploadId(uploadId);
            request.setPartSize(file.length());

            // 调用uploadPart接口上传段
            UploadPartResult upr = obsClient.uploadPart(request);
            logger.debug("Etag: {}, PartNumber: {}", upr.getEtag(), upr.getPartNumber());
        } catch (ObsException e) {
            logger.error("Error message: {}. ResponseCode: {}", e.getErrorMessage(), e.getResponseCode());
        } catch (NoSuchAlgorithmException | IOException e) {
            logger.error("Upload part failed. Error message: {}", e.getMessage());
        }
    }


    /**
     * 列出段
     */
    public static void listPart(ObsClient obsClient, ListPartsRequest request) {
        try {
            // 调用listParts接口列出Id为listMultipartUploads
            // 的上传任务的所有段
            ListPartsResult partsList = obsClient.listParts(request);

            List<Multipart> parts = partsList.getMultipartList();
            for (Multipart multipart : parts) {
                logger.debug("Etag: {} , PartNumber: {} , Size: {}", multipart.getEtag(), multipart.getPartNumber(), multipart.getSize());
            }
        } catch (ObsException e) {
            logger.error("Error message: {}. ResponseCode: {}", e.getMessage(), e.getResponseCode());
        }
    }

    /**
     * 列出多段上传任务
     */
    public static void listMultipartUploads(ObsClient obsClient, ListMultipartUploadsRequest request) {
        try {
            // 调用listMultipartUploads方法列出上传任务的信息
            MultipartUploadListing listing = obsClient.listMultipartUploads(request);
            List<MultipartUpload> uploads = listing.getMultipartTaskList();
            for (MultipartUpload multipartUpload : uploads) {
                System.out.println("BucketName: " + multipartUpload.getBucketName() + ",ObjectKey: "
                        + multipartUpload.getObjectKey() + ",UploadId: " + multipartUpload.getUploadId() + ", Initiator: "
                        + multipartUpload.getInitiator().getId());
            }
        } catch (ObsException e) {
            System.out.println("Error message: " + e.getErrorMessage() + ". ResponseCode: " + e.getResponseCode());
        }
    }

    /**
     * 合并段
     */
    private static void completeMultipartUpload(ObsClient obsClient) {
        try {
            // 封装请求消息
            CompleteMultipartUploadRequest request = new CompleteMultipartUploadRequest();
            request.setBucketName("doc-bucket");// 设置桶名
            request.setObjectKey("2012-sales-report.pptx");// 设置对象名

            // 要合并的段
            PartEtag partEtag1 = new PartEtag();
            partEtag1.seteTag("80da701f668935da22c0db8ad72c730b");
            partEtag1.setPartNumber(1);
            PartEtag partEtag2 = new PartEtag();
            partEtag2.seteTag("772c83937dbb11d6bb49f9c4e5ca5308");
            partEtag2.setPartNumber(2);
            PartEtag partEtag3 = new PartEtag();
            partEtag3.seteTag("e81ba3d4c5791eeb82ec6369b647f34d");
            partEtag3.setPartNumber(3);

            // 要合并的段集合
            List<PartEtag> partEtags = new ArrayList<PartEtag>();
            partEtags.add(partEtag1);
            partEtags.add(partEtag2);
            partEtags.add(partEtag3);
            request.setPartEtag(partEtags);
            request.setUploadId("A506C2FAC3ADC84B59F10CEF333F6065");

            // 调用completeMultipartUpload接口，合并已上传的段
            CompleteMultipartUploadResult result = obsClient.completeMultipartUpload(request);
            System.out.println("ObjectKey: " + result.getObjectKey() + ", Etag: " + result.getEtag());
        } catch (ObsException e) {
            System.out.println("Complete uploading failed. Error message: " + e.getErrorMessage() + " ResponseCode: "
                    + e.getResponseCode());
        }
    }

    /**
     * 取消多段上传任务
     */
    private static void abortMultipartUpload(ObsClient obsClient) {
        try {
            // 封装取消上传任务的请求
            AbortMultipartUploadRequest request = new AbortMultipartUploadRequest();
            request.setBucketName("image-bucket");
            request.setObjectKey("hd001.jpeg");
            request.setUploadId("72BBAB6DBD2D365764B5F06B677C6DF6");
            // 调用abortMultipartUpload接口，取消上传任务
            obsClient.abortMultipartUpload(request);
            System.out.println(" Abort multi part upload task success.");
        } catch (ObsException e) {
            System.out.println("Abort multipart upload failed. Error message: " + e.getErrorMessage()
                    + ". ResponseCode: " + e.getResponseCode());
        }
    }

    /**
     * 设置桶Cors
     */

    private static void putBucketCors(ObsClient obsClient) {
        BucketCorsRule cors = new BucketCorsRule();
        cors.setId("123456789");

        List<String> listMothedList = new ArrayList<String>();
        listMothedList.add("GET");
        listMothedList.add("PUT");
        listMothedList.add("POST");

        List<String> listOrigin = new ArrayList<String>();
        listOrigin.add("obs.huawei.com");

        cors.setAllowedMethod(listMothedList);
        cors.setAllowedOrigin(listOrigin);

        S3BucketCors s3BucketCors = new S3BucketCors();
        List<BucketCorsRule> rules = new ArrayList<BucketCorsRule>();
        rules.add(cors);
        s3BucketCors.setRules(rules);
        try {

            obsClient.setBucketCors("123456-10", s3BucketCors);
        } catch (ObsException e) {
            System.out.println("Put Bucket Cors failed. Error message: " + e.getErrorMessage() + ". ResponseCode: "
                    + e.getResponseCode());
        }
    }

    /**
     * 获取桶的Cors
     *
     * @param obsClient
     */
    private static void getBucketCors(ObsClient obsClient) {
        try {
            S3BucketCors s3BucketCors = obsClient.getBucketCors("123456-10");
            for (BucketCorsRule cors : s3BucketCors.getRules()) {
                System.out.printf("ID:%s\t MAXAGE:%d\n", cors.getId(), cors.getMaxAgeSecond());
                List<String> list = cors.getAllowedHeader();
                System.out.println("AllowedHeader:");
                for (int i = 0; list != null && i < list.size(); i++) {
                    System.out.printf("%d:%s\n", i, list.get(i));
                }
                list = cors.getAllowedMethod();
                System.out.println("AllowedMethod:");
                for (int i = 0; list != null && i < list.size(); i++) {
                    System.out.printf("%d:%s\n", i, list.get(i));
                }
                list = cors.getAllowedOrigin();
                System.out.println("AllowedOrigin:");
                for (int i = 0; list != null && i < list.size(); i++) {
                    System.out.printf("%d:%s\n", i, list.get(i));
                }
                list = cors.getExposeHeader();
                System.out.println("ExposeHeader:");
                for (int i = 0; list != null && i < list.size(); i++) {
                    System.out.printf("%d:%s\n", i, list.get(i));
                }
            }

        } catch (ObsException e) {
            System.out.println("get bucket Cors failed. Error message: " + e.getErrorMessage() + ". ResponseCode: "
                    + e.getResponseCode());
        }
    }

    /**
     * 删除桶的Cors
     *
     * @param obsClient
     */
    private static void deleteBucketCors(ObsClient obsClient) {
        try {
            obsClient.deleteBucketCors("123456-10");
        } catch (ObsException e) {
            System.out.println("delete bucket Cors failed. Error message: " + e.getErrorMessage() + ". ResponseCode: "
                    + e.getResponseCode());
        }

    }

    /**
     * Options 桶
     */

    private static void OptionsBucket(ObsClient obsClient) {

        OptionsInfoRequest option = new OptionsInfoRequest();
        option.setOrigin("obs.huawei.com");
        List<String> headersList = new ArrayList<String>();
        headersList.add("http://www.baidu.com");

        List<String> methoedList = new ArrayList<String>();
        methoedList.add("PUT");
        methoedList.add("POST");

        //		option.setRequestHeaders(headersList);
        option.setRequestMethod(methoedList);

        try {
            OptionsInfoResult output = obsClient.optionsBucket("123456-10", option);
            System.out.printf("Origin:%s\tMaxAge:%d\n", output.getAllowOrigin(), output.getMaxAge());
            List<String> list = output.getAllowHeaders();
            System.out.println("AllowedHeader:");
            for (int i = 0; list != null && i < list.size(); i++) {
                System.out.printf("%d:%s\n", i, list.get(i));
            }

            list = output.getAllowMethods();
            System.out.println("AllowedMethod:");
            for (int i = 0; list != null && i < list.size(); i++) {
                System.out.printf("%d:%s\n", i, list.get(i));
            }

            list = output.getExposeHeaders();
            System.out.println("ExposeHeader:");
            for (int i = 0; list != null && i < list.size(); i++) {
                System.out.printf("%d:%s\n", i, list.get(i));
            }
        } catch (ObsException e) {
            System.out.println("option failed. Error message: " + e.getErrorMessage() + ". ResponseCode: "
                    + e.getResponseCode());
        }

    }

    /**
     * Options  对象
     */

    private static void OptionsObject(ObsClient obsClient) {

        OptionsInfoRequest option = new OptionsInfoRequest();
        option.setOrigin("obs.huawei.com");
        List<String> headersList = new ArrayList<String>();
        headersList.add("http://www.baidu.com");

        List<String> methoedList = new ArrayList<String>();
        methoedList.add("PUT");
        methoedList.add("POST");
        methoedList.add("HEAD");

        //		option.setRequestHeaders(headersList);
        option.setRequestMethod(methoedList);

        try {
            OptionsInfoResult output = obsClient.optionsObject("123456-10", "", option);
            System.out.printf("Origin:%s\tMaxAge:%d\n", output.getAllowOrigin(), output.getMaxAge());
            List<String> list = output.getAllowHeaders();
            System.out.println("AllowedHeader:");
            for (int i = 0; list != null && i < list.size(); i++) {
                System.out.printf("%d:%s\n", i, list.get(i));
            }

            list = output.getAllowMethods();
            System.out.println("AllowedMethod:");
            for (int i = 0; list != null && i < list.size(); i++) {
                System.out.printf("%d:%s\n", i, list.get(i));
            }

            list = output.getExposeHeaders();
            System.out.println("ExposeHeader:");
            for (int i = 0; list != null && i < list.size(); i++) {
                System.out.printf("%d:%s\n", i, list.get(i));
            }
        } catch (ObsException e) {
            System.out.println("option object  failed. Error message: " + e.getErrorMessage() + ". ResponseCode: "
                    + e.getResponseCode());
        }

    }

    /**
     * 上传对象
     */
    public static void putObject(byte[] putObjBytes, ObsClient obsClient, String bucketName, String objectKey) throws ObsException {

        logger.debug("begin to put objectKey : {} @ {} ", objectKey, bucketName);

        ObjectMetadata metadata = new ObjectMetadata();// 设置上传对象的元数据
        ByteArrayInputStream picIs = new ByteArrayInputStream(putObjBytes);
        // 封装上传对象的请求
        PutObjectRequest request = new PutObjectRequest();
        request.setBucketName(bucketName);
        request.setObjectKey(objectKey);
        request.setInput(picIs);
        metadata.setContentLength((long) putObjBytes.length);// 设置头信息中的文件长度
        metadata.setContentType("image/jpeg");// 设置上传的文件类型
        request.setMetadata(metadata);
        request.setObjectKey(objectKey);

        // 调用putObject接口创建对象
        PutObjectResult result = obsClient.putObject(request);
        logger.debug("Put object success. objectKey : {} @ {} ", objectKey, bucketName);
    }

}
