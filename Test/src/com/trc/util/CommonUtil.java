/**   
* @Title: CommonUtil.java 
* @Package com.hoo.util 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 吴东雄
* @date 2016年3月16日 下午10:48:14 
* Copyright (c) 2016, 杭州海适云承科技有限公司 All Rights Reserved.
* @version V1.0   
*/
package com.trc.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import net.sf.json.JSONObject;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trc.exception.DataException;

/** 
 * @ClassName: CommonUtil 
 * @Description: TODO
 * @author 吴东雄
 * @date 2016年3月16日 下午10:48:14 
 *  
 */
public class CommonUtil {
	
	private static Log log = LogFactory.getLog(CommonUtil.class);

	public static final String HTTP_SERVLET_REQUEST = "HttpServletRequest";
	public static final String MODEL_MAP = "ModelMap";
	
	public static Map<String, Object> getMap(String key, String Object){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(key, Object);
		return map;
	}
	
	
	/**
	 * json转javaBean
	 * @param param
	 * @param clasz
	 * @return
	 */
	public static Object getBean(JSONObject param, Class<?> clasz){
		return JSONObject.toBean(param, clasz);
	}
	
	/**
	 * json转map
	 * @param json
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String,Object> jsonToMap(JSONObject json){
		Map<String,Object> map = new HashMap<String,Object>();
		Iterator<String> it = json.keys();  
	    while(it.hasNext()) {  
	        String key = (String) it.next();  
	        map.put(key, json.get(key));  
	    }  
		return map;
	}
	
    /** 
     * 讲json里的字段值设置到java对象
     * @param type 要转化的类型 
     * @param map 包含属性值的 map 
     * @return 转化出来的 JavaBean 对象 
     * @throws IntrospectionException 
     *             如果分析类属性失败 
     * @throws IllegalAccessException 
     *             如果实例化 JavaBean 失败 
     * @throws InstantiationException 
     *             如果实例化 JavaBean 失败 
     */ 
    public static void setBeanProperty(Object obj, JSONObject json){ 
    	Map<String,Object> map = jsonToMap(json);
    	String propName = "";
    	Object propVal = "";
    	BeanInfo beanInfo = null;
		try {
			beanInfo = Introspector.getBeanInfo(obj.getClass()); // 获取类属性
			// 创建 JavaBean 对象 
			// 给 JavaBean 对象的属性赋值
			PropertyDescriptor[] propertyDescriptors = beanInfo
					.getPropertyDescriptors();
			for (int i = 0; i < propertyDescriptors.length; i++) {
				PropertyDescriptor descriptor = propertyDescriptors[i];
				propName = descriptor.getName();
				if (map.containsKey(propName)) {
					// 下面一句可以 try 起来，这样当一个属性赋值失败的时候就不会影响其他属性赋值。
					propVal = map.get(propName);
					//处理数字类型
					if(StringUtils.equals(Double.class.getName(), descriptor.getPropertyType().getName())){
						if(propVal instanceof String){
							if(StringUtils.isNotEmpty(propVal.toString()))
								propVal = Double.parseDouble((String)propVal);
							else
								propVal = new Double(0);
						}
						else if(propVal instanceof Integer)
							propVal = ((Integer)propVal).doubleValue();
						else if(propVal instanceof Float)
							propVal = ((Float)propVal).doubleValue();
					}
					//处理字符串类型
					if(StringUtils.equals(String.class.getName(), descriptor.getPropertyType().getName())){
						propVal = String.valueOf(propVal);
					}
					if(null != propVal && StringUtils.isNotEmpty(propVal.toString())){
						Object[] args = new Object[1];
						args[0] = propVal;
						descriptor.getWriteMethod().invoke(obj, args);
					}
					
				}
			}
		}  catch (Exception e) {
			StringBuilder builder = new StringBuilder();
			builder.append(map)
					.append("设置java对象")
					.append(beanInfo.getBeanDescriptor().getBeanClass()
							.getName()).append("属性").append(propName)
					.append("的属性值").append(propVal).append("异常,异常信息:")
					.append(e.getMessage());
			log.error(builder.toString());
			throw new DataException(builder.toString(), e);
		} 
    } 

    
    /**
     * 拼接字符串
     * @param strs
     * @return
     */
    public static StringBuffer joinStr(String...strs){
    		StringBuffer buffer = new StringBuffer();
    	for(String str : strs){
    			buffer.append(str);
    	}
    	return buffer;
    }
    

	/**
	 * 
	* @Title: getMethodParams 
	* @Description: 获取类方法参数
	* @param @param calzz 类对象
	* @param @param methodName 方法名称
	* @param @return
	* @param @throws Exception    
	* @return String[] 参数名称数组
	* @throws
	 */
	public static String[] getMethodParams(Class<?> calzz, String methodName) throws Exception{
		ClassPool pool = ClassPool.getDefault();  
		CtClass cc = null;
		/*
		 * 此处异常处理是在被拦截的类已经被代理的情况下，获取到被代理的类
		 */
		try {
			pool.insertClassPath(new ClassClassPath(calzz));
			cc = pool.get(calzz.getName());  
		} catch (Exception e) {
			pool.insertClassPath(new ClassClassPath(calzz.getSuperclass()));
			cc =pool.get(calzz.getSuperclass().getName());  
		}
        CtMethod cm = cc.getDeclaredMethod(methodName);  
        //使用javaassist的反射方法获取方法的参数名  
        MethodInfo methodInfo = cm.getMethodInfo();  
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();  
        LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);  
        String[] paramNames = new String[cm.getParameterTypes().length];  
        int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;  
        for (int i = 0; i < paramNames.length; i++)
            paramNames[i] = attr.variableName(i + pos);  
		return paramNames;
	}
	

	/**
	 * 
	* @Title: converCollectionToString 
	* @Description: 将结合列表转换成字符串格式,用逗号","分割
	* @param @param list
	* @param @return    设定文件 
	* @return String    返回类型 
	* @throws
	 */
	public static String converCollectionToString(List<?> list){
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<list.size(); i++){
			if(i == (list.size()-1))
				sb.append(list.get(i).toString());
			else
				sb.append(list.get(i).toString()).append(",");
		}
		return sb.toString();
	}
	
}
