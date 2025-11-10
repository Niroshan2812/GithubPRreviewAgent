package org.niroshan.githubpragent.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/*
    1.1 - edit
    Validate webhook actually come from GitHub or not a random attacker
 */
@Service
public class WebhookSecurityService {

    @Value("${github.webhook.secret}")
    private String githubSecret;

    // validates the GitHub signature
    public boolean isValidSignature(String payload, String signature){
        try {
            // create an HMAC-SH256 MAC object
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(githubSecret.getBytes(), "HmacSHA256");
            mac.init(secretKey);

            // calculate the hash of the payload
            byte[] calculatedHashBytes  =  mac.doFinal(payload.getBytes());
            //format the hash code as a hex string
            String calculatedSignarute  = "sha256="+ HexFormat.of().formatHex(calculatedHashBytes);
            //Compare signature with the one from GitHub
            return calculatedSignarute.equals(signature);
        }catch (NoSuchAlgorithmException | InvalidKeyException e){
            e.printStackTrace();
            return false;
        }
    }
}
