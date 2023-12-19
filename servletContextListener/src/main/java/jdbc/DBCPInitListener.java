package jdbc;

import java.io.StringReader;
import java.sql.DriverManager;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.tomcat.dbcp.dbcp2.ConnectionFactory;
import org.apache.tomcat.dbcp.dbcp2.DriverManagerConnectionFactory;
import org.apache.tomcat.dbcp.dbcp2.PoolableConnection;
import org.apache.tomcat.dbcp.dbcp2.PoolableConnectionFactory;
import org.apache.tomcat.dbcp.dbcp2.PoolingDriver;
import org.apache.tomcat.dbcp.pool2.impl.GenericObjectPool;
import org.apache.tomcat.dbcp.pool2.impl.GenericObjectPoolConfig;

// ServletContextListener 인터페이스를 상속 받는다.
public class DBCPInitListener implements ServletContextListener{
	
	// 웹 어플리케이션이 종료될 때, servletContextListener 객체가 삭제되는 메소드
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub
		
	}

	// 1. 웹 어플리케이션이 종료될 때, servletContextListener 객체가 생성되는 메소드
	// 2. ServletContextEvent 클래스는 웹 어플리케이션 컨텍스트를 구할 수 있는 getServletContext()메소드를 제공한다.
	// 3. getServletContext()메소드가 리턴하는 ServletContext 객체는 JSP의 application 기본 객체와 동일한 객체로,
	// web.xml 파일에 설정된 컨텍스트 초기화 파라미터를 구할 수 있다.
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		// TODO Auto-generated method stub
		String poolconfig = 
			// 1. String getInitParameter() : 지정한 이름을 갖는 context 초기화 파리미터 값을 리턴. 
			// 존재하지 않을 경우 null을 리턴.
			// 파라미터로는 <param-name> 태그로 지정한 이름을 넣어주면 된다.
			sce.getServletContext().getInitParameter("poolConfig");
			
			//2. java.util.Enumeration<String> getInitParameterNames() : context 초기화 파라미터 이름 목록을 Enumeration타입으로 리턴.
				
		Properties prop = new Properties();
		try {
			// "키=값"형식으로 구성된 문자열로부터 프로퍼티를 로딩한다.
			// web.xml의 <context-param>의 poolconfig 초기화 파라미터 설정 값을 Properties객체에 프로퍼티로 등록을 한다.
			prop.load(new StringReader(poolconfig));
		} catch (Exception e) {
			// TODO: handle exception
			// contextInitialized()메소드 정의에 throws가 없어서 RunctimeException을 발생시킨다.
			throw new RuntimeException();
		}
		// JDBC드라이버 로드
		loadJDBCDriver(prop);
		// connectionPool 생성
		initConnectionPool(prop);
		
		
		
		System.out.println(poolconfig);
		
	}
	
	private void loadJDBCDriver(Properties prop) {
		// poolconfig 초기화 파라미터 설정 값 중 jdbcdriver 값.
		String driverClass = prop.getProperty("jdbcdriver");
		try {
			Class.forName(driverClass);
			
		} catch (ClassNotFoundException e) {
			// TODO: handle exception
			throw new RuntimeException("fail to load JDBC Driver", e);
		}
	}
	
	private void initConnectionPool(Properties prop) {
		try {
				// poolconfig 초기화 파라미터 설정 값 중 jdbcUrl, dbUser, dbPass 값.
				String jdbcUrl = prop.getProperty("jdbcUrl");
				String username = prop.getProperty("dbUser");
				String pw = prop.getProperty("dbPass");
				
				ConnectionFactory connFactory =
						new DriverManagerConnectionFactory(jdbcUrl, username, pw);
				
				PoolableConnectionFactory poolableConnFactory = 
						new PoolableConnectionFactory(connFactory, null);
				poolableConnFactory.setValidationQuery("select 1");
	
				GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
				poolConfig.setTimeBetweenEvictionRunsMillis(1000L * 60L * 5L);
				poolConfig.setTestWhileIdle(true);
				poolConfig.setMinIdle(5);
				poolConfig.setMaxTotal(50);
	
				GenericObjectPool<PoolableConnection> connectionPool = 
						new GenericObjectPool<>(poolableConnFactory, poolConfig);
				poolableConnFactory.setPool(connectionPool);
				
				Class.forName("org.apache.commons.dbcp2.PoolingDriver");
				PoolingDriver driver = (PoolingDriver)
					DriverManager.getDriver("jdbc:apache:commons:dbcp:");
				String poolName = prop.getProperty("poolName");
				driver.registerPool(poolName, connectionPool);
		} catch (Exception e) {
			// TODO: handle exception
			throw new RuntimeException(e);
		}
		
		
	}
	
}
