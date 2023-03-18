package com.poc.qrapp.QRService;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.poc.qrapp.Redis.RedisConfig;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Service
public class QRService {

    private final HashOperations<String, Object, Object> hashOperations = RedisConfig.redisTemplate().opsForHash();
    private static final String SUCCESS = "success";
    private static final String PENDING = "pending";
    private static final String HASH_KEY = "LOGIN";
    private static final String INVALID_MSG = "Invalid Request. Please try again.";

    public String generateQrCode (String host, Model model) {

        try {

            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            String refId = UUID.randomUUID().toString();
            hashOperations.put(HASH_KEY, refId, PENDING);

            BitMatrix bitMatrix = qrCodeWriter.encode(host + "/" + refId, BarcodeFormat.QR_CODE, 300, 300);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            MatrixToImageConfig config = new MatrixToImageConfig(0xFF000002, 0xFFFFFFFF);

            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", byteArrayOutputStream, config);

            model.addAttribute("qrCode", Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray()));
            model.addAttribute("refId", refId);
            return "index";

        }  catch (WriterException e) {
            return showInfoPage(0, "Failed to Generate QR", model);
        } catch (IOException e) {
            return showInfoPage(0, "Something went wrong", model);
        }
    }

    public String login(String refId, Model model) {
        if (isRefIdNotPresentInRedis(refId)) {
            return showInfoPage(0, INVALID_MSG, model);
        }
        hashOperations.put(HASH_KEY, refId, SUCCESS);

       return showInfoPage(1, "Your are logged In successfully....", model);
    }

    public ResponseEntity<?> pollData(String refId) {
       String value =  String.valueOf(hashOperations.get(HASH_KEY, refId));
       return SUCCESS.equals(value) ? new ResponseEntity<>(HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    public String loadLoginForm (String refId, Model model) {
        if (isRefIdNotPresentInRedis(refId)) {
            return showInfoPage(0, INVALID_MSG, model);
        }

        model.addAttribute("refId", refId);
        return "login-form";
    }

    public String redirect(String refId, Model model) {
        String value = String.valueOf(hashOperations.get(HASH_KEY, refId));

        if (isRefIdNotPresentInRedis(refId)) {
            return showInfoPage(0, INVALID_MSG, model);
        }

        if (SUCCESS.equals(value)) {
            model.addAttribute("flag", 1);
            model.addAttribute("msg", "Your are logged In successfully....");
            return "info";
        }

        model.addAttribute("flag", 0);
        model.addAttribute("msg", "Failed to log in. Please try again");
        return "info";

    }

    private String showInfoPage (int flag, String msg, Model model) {

        model.addAttribute("flag", flag);
        model.addAttribute("msg", msg);
        return "info";
    }

    private boolean isRefIdNotPresentInRedis(String refId) {
        return hashOperations.get(HASH_KEY, refId) == null;
    }

}
