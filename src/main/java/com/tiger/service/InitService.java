package com.tiger.service;

import com.tiger.mapper.oldDb.OldMapper;
import com.tiger.model.BZaFjxx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Package: com.tiger.service
 * ClassName: InitService
 * Author: Tiger
 * Description:
 * CreateDate: 2016/10/8
 * Version: 1.0
 */
@Service
public class InitService {

    private static Logger logger = LoggerFactory.getLogger(InitService.class);


    @Autowired
    private OldMapper oldUserMapper;

    public void initData() throws IOException {

        URL xmlpath = this.getClass().getClassLoader().getResource("");
        File f = new File(xmlpath.getPath() + "/pic/1.jpg");
        BufferedImage bi = ImageIO.read(f);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bi,"jpg",byteArrayOutputStream);

        byte[] pic = Arrays.copyOf(byteArrayOutputStream.toByteArray(),byteArrayOutputStream.toByteArray().length);

        /*
        * MYSQL:
        * max_allowed_packet = 200M
        * */
        for(int i = 0;i<1;i++){
            /*
        * JVM: -xss1g
        * */
//            ArrayList<BRyzp> arrayList = new ArrayList();
//            for(int j = 0;j<500;j++){
//                BRyzp bRyzp = new BRyzp();
//                bRyzp.setSystemid(UUID.randomUUID().toString());
//                bRyzp.setPhoto(pic);
//                arrayList.add(bRyzp);
//            }
//            oldUserMapper.BatchInsertBRyzp(arrayList);

            ArrayList<BZaFjxx> arrayList = new ArrayList();
            for(int j = 0;j<1;j++){
                BZaFjxx bZaFjxx = new BZaFjxx();
                bZaFjxx.setSystemid("testtmp");
                bZaFjxx.setFjnr(pic);
                arrayList.add(bZaFjxx);
            }
            oldUserMapper.BatchInsertBZaFjxx(arrayList);
        }

//        BRyzp u = oldUserMapper.findBRyzpById(2);
//
//        File f2 = new File(xmlpath.getPath() + "/res.jpg");
//        FileOutputStream fos =new FileOutputStream(f2);
//        fos.write(u.getPic());
//        fos.flush();
//        fos.close();

    }




}
