package br.order.user.controller.filter;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import br.crm.common.utils.JsonUtils;
import br.order.common.utils.HttpRequestResolve;
import br.order.user.vo.empUser.CustomerRegistVo;

public class SysInfoFilter implements HandlerInterceptor {

	private String reqeustUrl;
	private final Logger logger = LoggerFactory.getLogger("");
	private static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ssss");
	
	// 重写 preHandle()方法，在业务处理器处理请求之前对该请求进行拦截处理
	/**
	 * (非 Javadoc)
	 * <p>Title: preHandle</p>
	 * <p>Description: </p>
	 * @param request
	 * @param response
	 * @param handler
	 * @return
	 * @throws Exception
	 * @see org.springframework.web.servlet.HandlerInterceptor#preHandle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object)
	 * 
	 *  预处理回调方法，实现处理器的预处理（如登录检查），第三个参数为响应的处理器；
		返回值：true表示继续流程（如调用下一个拦截器或处理器）；
		false表示流程中断（如登录检查失败），不会继续调用其他的拦截器或处理器，此时我们需要通过response来产生响应；
		
	 * preHandle方法是进行处理器拦截用的，顾名思义，该方法将在Controller处理之前进行调用，SpringMVC中的Interceptor拦截器是链式的，可以同时存在 
     * 多个Interceptor，然后SpringMVC会根据声明的前后顺序一个接一个的执行，而且所有的Interceptor中的preHandle方法都会在 
     * Controller方法调用之前调用。SpringMVC的这种Interceptor链式结构也是可以进行中断的，这种中断方式是令preHandle的返 
     * 回值为false，当preHandle的返回值为false的时候整个请求就结束了。 
	* loginStatus 0 已登录  1 个人登录  2企业登录   	
	 */
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response,Object handle)
			throws Exception {
		System.out.println("preHandle......");
		reqeustUrl = request.getRequestURI();  
		logger.info("preHandle Request--> URL is "+reqeustUrl+",parameter is "+HttpRequestResolve.getRequestBody(request));
		System.out.println((new Date())+" preHandle Request--> URL is "+reqeustUrl+",parameter is "+HttpRequestResolve.getRequestBody(request));
		
	    HttpServletRequest req = (HttpServletRequest) request; //在这儿读取cookie 
	    HttpSession ss = req.getSession(); 
	    
	    Cookie[] cs = req.getCookies();
	    //判断cookie有没有登陆 
	    logger.info("preHandle......cs开始"+cs);
	    if(cs!=null){
	    	//是否登录
	    	System.err.println("preHandle......cs02 cookie  not null"); 
	    	if(ss.getAttribute("loginUser")==null){
	    		System.err.println("preHandle......cs03 loginUser is null"); 
	    		String autoLoginVule=null,loginType=null;
	    		for(Cookie c:cs){ 
	    			 if(c.getName().equals("autoLogin")){
	    				 autoLoginVule=c.getValue();
	    				 System.err.println("preHandle......cs04 autoLogin："+autoLoginVule); 
	    			 }
	    			 if(c.getName().equals("loginType")){
	    				 loginType=c.getValue();
	    				System.err.println("preHandle......cs05 loginType："+loginType); 
	    			 }  
	    		} 
	    		//判断用户登录状态
	    		  if(autoLoginVule!=null){//如果存在自动登录的cookie  
	    		       CustomerRegistVo customerRegistVo= JsonUtils.jsonToPojo(autoLoginVule,CustomerRegistVo.class);
	    		       System.out.println("preHandle......cs06自动登陆成功"+customerRegistVo);
	    		      req.getSession().setAttribute("loginUser",customerRegistVo);  
	    		      System.out.println("preHandle......cs07自动登陆成功，并且添加session value"+customerRegistVo); 
	    		      return true;
	    		   } 
		    }else {
		    	  System.err.println("preHandle......cs10 session 已登陆");  
		    	  return true;
			}  
	    } 
	    return true;
	}

	/**
	 * (非 Javadoc)
	 * <p>Title: postHandle</p>
	 * <p>Description: </p>
	 * @param request
	 * @param response
	 * @param handler
	 * @param modelAndView
	 * @throws Exception
	 * @see org.springframework.web.servlet.HandlerInterceptor#postHandle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.web.servlet.ModelAndView)
	 * 后处理回调方法，实现处理器的后处理（但在渲染视图之前），此时我们可以通过modelAndView（模型和视图对象）对模型数据进行处理或对视图进行处理，modelAndView也可能为null。
	 * 
	 * 
	 * 这个方法只会在当前这个Interceptor的preHandle方法返回值为true的时候才会执行。postHandle是进行处理器拦截用的，它的执行时间是在处理器进行处理之 
     * 后，也就是在Controller的方法调用之后执行，但是它会在DispatcherServlet进行视图的渲染之前执行，也就是说在这个方法中你可以对ModelAndView进行操 
     * 作。这个方法的链式结构跟正常访问的方向是相反的，也就是说先声明的Interceptor拦截器该方法反而会后调用，这跟Struts2里面的拦截器的执行过程有点像， 
     * 只是Struts2里面的intercept方法中要手动的调用ActionInvocation的invoke方法，Struts2中调用ActionInvocation的invoke方法就是调用下一个Interceptor 
     * 或者是调用action，然后要在Interceptor之前调用的内容都写在调用invoke之前，要在Interceptor之后调用的内容都写在调用invoke方法之后。 
	 * 
	 */
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		// TODO Auto-generated method stub
		
	}

	/**
	 * (非 Javadoc)
	 * <p>Title: afterCompletion</p>
	 * <p>Description: </p>
	 * @param request
	 * @param response
	 * @param handler
	 * @param ex
	 * @throws Exception
	 * @see org.springframework.web.servlet.HandlerInterceptor#afterCompletion(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, java.lang.Exception)
	 * 整个请求处理完毕回调方法，即在视图渲染完毕时回调，如性能监控中我们可以在此记录结束时间并输出消耗时间，还可以进行一些资源清理，类似于try-catch-finally中的finally，但仅调用处理器执行链中preHandle返回true的拦截器的afterCompletion。
	 * 
	 * 该方法也是需要当前对应的Interceptor的preHandle方法的返回值为true时才会执行。该方法将在整个请求完成之后，也就是DispatcherServlet渲染了视图执行， 
     * 这个方法的主要作用是用于清理资源的，当然这个方法也只能在当前这个Interceptor的preHandle方法的返回值为true时才会执行。 
	 */
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

}
