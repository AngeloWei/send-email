package com.myemail.handle;

import com.sun.mail.util.MailSSLSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.Properties;

@Component
@EnableScheduling
public class SendEmail {

    private  static String key="elkzmzoeglcvbahf" ;

    private  String filePath="/home/centos/sql_result";

    private String sender="695515832@qq.com";

    private String receiver="1205110565@qq.com";

    private  Logger log = LoggerFactory.getLogger(this.getClass());
    @Scheduled(cron = "5 31 * * * ? ")
    public void sendEmail() throws Exception {
        log.info("开始执行send....");
        Properties prop = new Properties();
        prop.setProperty("mail.host", "smtp.qq.com"); //// 设置QQ邮件服务器
        prop.setProperty("mail.transport.protocol", "smtp"); // 邮件发送协议
        prop.setProperty("mail.smtp.auth", "true"); // 需要验证用户名密码

        // 关于QQ邮箱，还要设置SSL加密，加上以下代码即可
        MailSSLSocketFactory sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);
        prop.put("mail.smtp.ssl.enable", "true");
        prop.put("mail.smtp.ssl.socketFactory", sf);

        //使用JavaMail发送邮件的5个步骤

        //创建定义整个应用程序所需的环境信息的 Session 对象

        Session session = Session.getDefaultInstance(prop, new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                //发件人邮件用户名、授权码
                return new PasswordAuthentication(sender, key);
            }
        });


        //开启Session的debug模式，这样就可以查看到程序发送Email的运行状态
        session.setDebug(true);

        //创建邮件对象
        MimeMessage message = complexEmail(session, filePath);
        if (message == null) {
            return;
        }
        //2、通过session得到transport对象
        Transport ts = session.getTransport();

        //3、使用邮箱的用户名和授权码连上邮件服务器
        ts.connect("smtp.qq.com", sender, key);

        //4、创建邮件
        ts.sendMessage(message, message.getAllRecipients());

        ts.close();

    }

    public  MimeMessage complexEmail(Session session,String filePath) throws MessagingException {
        //消息的固定信息
        MimeMessage mimeMessage = new MimeMessage(session);

        //发件人
        mimeMessage.setFrom(new InternetAddress(sender));
        //收件人
        mimeMessage.setRecipient(Message.RecipientType.TO,new InternetAddress(receiver));
        //邮件标题
        mimeMessage.setSubject("今日统计表");


        //附件
        //拼接附件
        MimeMultipart allFile = new MimeMultipart();
        File file = new File(filePath);
        File[] files = file.listFiles();
        if (files.length == 0) {
            return null;
        } else {
            for (int i = 0; i < files.length; i++) {
                MimeBodyPart appendix = new MimeBodyPart();
                String absolutePath = files[i].getAbsolutePath();
                appendix.setDataHandler(new DataHandler(new FileDataSource(absolutePath)));
                appendix.setFileName(files[i].getName());
                allFile.addBodyPart(appendix);//附件
                files[i].deleteOnExit();
            }
        }
        allFile.setSubType("mixed"); //正文和附件都存在邮件中，所有类型设置为mixed
        //放到Message消息中
        mimeMessage.setContent(allFile);
        mimeMessage.saveChanges();//保存修改
        return mimeMessage;
    }

}
