package com.gaofans.vertx.gateway.support;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;

/**
 * @author GaoFans
 */
public class UriUtil {

    public final static char SEPARATOR = '/';

    public static final PathMatcher PATH_MATCHER = new AntPathMatcher();

    /**
     * 拼接多段uri
     * @param uri
     * @return
     */
    public static String concatRouteUri(String... uri){
        StringBuilder sb = new StringBuilder();
        sb.append(SEPARATOR);
        for (String s : uri) {
            for (char c : s.toCharArray()) {
                if(c == SEPARATOR && sb.charAt(sb.length() - 1) == SEPARATOR){
                    continue;
                }
                sb.append(c);
            }
            if(sb.charAt(sb.length() - 1) != SEPARATOR){
                sb.append(SEPARATOR);
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public static boolean match(String pattern, String path){
        if(!PATH_MATCHER.isPattern(pattern)){
            throw new IllegalArgumentException("参数不正确");
        }
        return PATH_MATCHER.match(pattern,path);
    }

    public static boolean matchOneOf(String[] patterns,String path){
        for (String pattern : patterns) {
            if(PATH_MATCHER.match(pattern,path)){
                return true;
            }
        }
        return false;
    }

}
