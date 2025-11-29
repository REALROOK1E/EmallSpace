package com.emallspace.marketing.service.impl;

import org.roaringbitmap.RoaringBitmap;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserPortraitService {

    // Mock storage for Bitmaps (Tag -> Bitmap)
    private Map<String, RoaringBitmap> tagBitmaps = new HashMap<>();

    public UserPortraitService() {
        // Initialize with some dummy data
        RoaringBitmap male = new RoaringBitmap();
        male.add(1, 2, 3, 5, 8);
        tagBitmaps.put("gender:male", male);

        RoaringBitmap active30d = new RoaringBitmap();
        active30d.add(1, 3, 5, 7, 9);
        tagBitmaps.put("behavior:active_30d", active30d);
        
        RoaringBitmap highSpender = new RoaringBitmap();
        highSpender.add(1, 2, 9, 10);
        tagBitmaps.put("consumption:high", highSpender);
    }

    /**
     * Select users based on boolean logic.
     * Example: (Male AND Active) OR HighSpender
     */
    public RoaringBitmap selectUsers(String logicExpression) {
        // In a real system, we would parse a JSON tree or expression.
        // Here we simulate a specific hardcoded logic for demonstration:
        // "gender:male AND behavior:active_30d"
        
        RoaringBitmap result = new RoaringBitmap();
        
        // 1. Load Bitmaps (Simulate loading from Minio/Cache)
        RoaringBitmap male = tagBitmaps.getOrDefault("gender:male", new RoaringBitmap());
        RoaringBitmap active = tagBitmaps.getOrDefault("behavior:active_30d", new RoaringBitmap());

        // 2. Perform Bitwise AND
        RoaringBitmap intersection = RoaringBitmap.and(male, active);
        
        return intersection;
    }

    public byte[] serializeBitmap(RoaringBitmap bitmap) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        bitmap.serialize(dos);
        return bos.toByteArray();
    }
    
    public RoaringBitmap deserializeBitmap(byte[] data) throws IOException {
        RoaringBitmap bitmap = new RoaringBitmap();
        bitmap.deserialize(ByteBuffer.wrap(data));
        return bitmap;
    }
}
