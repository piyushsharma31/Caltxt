
package com.jovistar.commons.util;

/**
 * This class contains some functions
 * that are common during color manipulation
 */
public final class ColorUtils
{
    public static final int blend(int c1, int c2, int value256)
    {
        
        int v1 = value256 & 0xFF;
        int v2 = 255 - v1;
                
        
        // FAST VERSION
        int c1_RB = c1 & 0x00FF00FF;
        int c2_RB = c2 & 0x00FF00FF;
        int c1_AG = (c1 >>> 8) & 0x00FF00FF;
        int c2_AG = (c2 >>> 8) & 0x00FF00FF;
        
        return
              (((c1_RB * v1 + c2_RB * v2) >> 8) & 0x00FF00FF) |
              ((c1_AG * v1 + c2_AG * v2) & 0xFF00FF00);
        /*
        // SLOW VERSION
        int a1 = (c1 >> 24) & 0xFF;
        int r1 = (c1 >> 16) & 0xFF;
        int g1 = (c1 >>  8) & 0xFF;
        int b1 = (c1 >>  0) & 0xFF;
        
        int a2 = (c2 >> 24) & 0xFF;
        int r2 = (c2 >> 16) & 0xFF;
        int g2 = (c2 >>  8) & 0xFF;
        int b2 = (c2 >>  0) & 0xFF;
        
        int a = (a1 * v1 + a2 * v2) >> 8;
        int r = (r1 * v1 + r2 * v2) >> 8;
        int g = (g1 * v1 + g2 * v2) >> 8;
        int b = (b1 * v1 + b2 * v2) >> 8;
        
        return (a << 24) | (r << 16) | (g << 8) | b;
       */
    }
    
        
    public static int darker(int c)
    {
        int a = (c >> 24) & 0xFF;
        int r = (c >> 16) & 0xFF;
        int g = (c >>  8) & 0xFF;
        int b = (c >>  0) & 0xFF;
        
        r = (r * 15) >> 4;
        g = (g * 15) >> 4;
        b = (b * 15) >> 4;
        
        return (a << 24) | (r << 16) | (g << 8) | b;        
    }
    
    public static int lighter(int c)
    {
        int a = (c >> 24) & 0xFF;
        int r = (c >> 16) & 0xFF;
        int g = (c >>  8) & 0xFF;
        int b = (c >>  0) & 0xFF;
        
        r = Math.max(1, Math.min(255, (r * 17) >> 4));
        g = Math.max(1, Math.min(255, (g * 17) >> 4));
        b = Math.max(1, Math.min(255, (b * 17) >> 4));
        
        
        return (a << 24) | (r << 16) | (g << 8) | b;        
    }
    
    // ----------------------------------------------
    public static int mix(int c1, int c2)
    {
        // return blend(c1, c2, 0x7f);
        
        int c_RB = (((c1 & 0x00FF00FF) + (c2 & 0x00FF00FF)) >> 1) & 0x00FF00FF;
        int c_AG = (((c1 & 0xFF00FF00) >>> 1) + ((c2 & 0xFF00FF00) >>> 1)) & 0xFF00FF00;
        return c_RB | c_AG;
    }
    
    public static int mix(int c1, int c2, int c3, int c4)
    {
        // return blend(c1, c2, 0x7f);
        
        int c_RB = (
                  ((c1 & 0x00FF00FF) + (c2 & 0x00FF00FF) + (c3 & 0x00FF00FF) + (c4 & 0x00FF00FF)) >> 2
                  ) & 0x00FF00FF;
        
        int c_AG = (
                  ((c1 & 0xFF00FF00) >>> 2) + ((c2 & 0xFF00FF00) >>> 2) + 
                  ((c3 & 0xFF00FF00) >>> 2) + ((c4 & 0xFF00FF00) >>> 2) 
                  ) & 0xFF00FF00;
        return c_RB | c_AG;
    }
    
}
