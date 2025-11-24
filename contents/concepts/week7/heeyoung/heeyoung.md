# [App center] 7주차 - Logging & Swagger

# 🍀 Logging은 무엇이며 Spring에서 어떻게 적용할 수 있을까요?

## 1.  Logging 전략은 무엇이고 왜 세워야 할까요?

### **Logging 이란?**

서비스를 개발하거나 운영할 때 서비스의 에러를 파악하고, 디버깅하기 위해 사용합니다. 유저의 행동과 같은 다양한 정보들도 로그를 통해 기록하고 활용할 수 있습니다.

- **무엇을 기록할까?** (요청/응답? 오류? 비즈니스 이벤트?)
- **어디에 기록할까?** (Console? 파일? APM? Cloud?)
- **어떤 레벨로 기록할까?** (`INFO`, `WARN`, `ERROR` 등)
- **어떤 형식으로 남길까?** (JSON 로그? 텍스트 로그?)
- **어떻게 추적 가능하게 만들까?** (requestId? traceId?)

이러한 모든 계획을 묶어 놓은 것이 Logging 전략입니다.

## 2.  Log Level은 무엇이고 어떻게 적용해야 할까요?

Log Level 이란, 로그 메시지의 중요도를 나타내는 수준을 의미합니다. 로그 레벨은 로깅 시스템에서 사용되며, 로그 메시지의 중요도에 따라 해당 메시지를 기록할지 말지 결정합니다.

### `TRACE`

- 가장 상세한 로그 레벨로, 애플리케이션의 실행 흐름과 디버깅 정보를 기록합니다. 주로 디버깅 시에 사용됩니다.

### `DEBUG`

- 개발 환경에서 내부 로직 흐름을 추적하거나, 디버깅 시 세부 정보를 확인할 때 사용합니다.
- 너무 많은 로그를 남길 수 있으므로 운영 환경에서는 일반적으로 비활성화 합니다.

### `INFO`

- 애플리케이션의 정상 동작과 주요 이벤트를 기록합니다.
- 운영 환경에서도 남기는 경우가 많으며, 시스템 상태 파악에 도움이 됩니다.

### `WARN`

- 경고성 메시지를 기록합니다.
- 애플리케이션이 정상 동작하지만 주의가 필요한 상황을 알려줍니다.
- 잘못된 사용자 입력, 외부 API 호출 실패 등

### `ERROR`

- 오류 메시지를 기록합니다.
- 시스템에 치명적인 영향을 미치는 즉각적인 문제를 알립니다.

### `FATAL`

- 가장 심각한 오류 메시지를 기록합니다.

로그 레벨의 우선 순위는 아래와 같습니다.

```css
TRACE < DEBUG < INFO < WARN < ERROR < FATAL(있을 때)
```

만일 로그 레벨을 INFO 로 하면 DEBUG 와 TRACE 는 출력되지 않고, TRACE 로 하면 모든 레벨이 출력됩니다. 즉, **지정한 레벨의 상위 레벨의 로그만 출력**됩니다.

```css
// application.yml
logging:
  level:
    server.Heeyoung: debug
    org.springframework.web: debug
    org.springframework.security: debug
```

application.yml 에서 위와 같이 설정하면 해당 패키지와 스프링 MVC, 시큐리티 내부 동작이 모두 DEBUG 레벨까지 콘솔에 출력되게 됩니다.

```basic
2025-11-16T23:34:16.628+09:00 DEBUG 44917 --- [nio-8080-exec-2] o.s.w.s.m.m.a.HttpEntityMethodProcessor  : Using 'text/plain', given [*/*] and supported [text/plain, */*, application/json, application/*+json]
2025-11-16T23:34:16.629+09:00 DEBUG 44917 --- [nio-8080-exec-2] o.s.w.s.m.m.a.HttpEntityMethodProcessor  : Writing ["회원가입에 성공하였습니다."]
2025-11-16T23:34:16.633+09:00 DEBUG 44917 --- [nio-8080-exec-2] o.s.web.servlet.DispatcherServlet        : Completed 200 OK
```

## 3.  Spring에서 Logging을 적용하는 방법에는 어떤 것이 있나요?

### Logback

- Slf4j 의 구현체로, 스프링 부트 환경에서는 스프링 부트 의존성(spring-boot-starter-web)에 기본적으로 포함되어 있습니다.
- 실무에서 대부분 사용하는 라이브러리 입니다.

### Log4j2

- 최신 프레임워크로, Logback 에 비해 멀티 스레드 환경에서 높은 처리량을 가집니다.
    - 멀티 스레드 환경
        
        프로그램 하나가 여러 개 일을 동시에 하는 것을 멀티 스레드라고 합니다.
        
        웹 서버는 동시에 여러 요청이 들어 여러 스레드가 병렬로 실행되기 때문에 멀티 스레드 환경입니다.
        

### SLF4J (Simple Logging Facade For Java)

**SLF4J 라이브러리**는 이들을 통합하여 인터페이스로 제공합니다. 로그를 남기기 위해선 항상 Logger 변수를 선언해야 하는데, Lombok 의 @Slf4j 어노테이션을 사용하면 자동으로 log 변수를 선언해 편리하게 log 를 찍을 수 있다.

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;

    // 메뉴 등록
    @Transactional
    public MenuResponseDto createMenu(MenuCreateRequestDto dto, Long storeId) {

        log.debug("메뉴 등록 요청 들어옴. storeId={}, dto={}", storeId, dto);

        // 가게 조회
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> {
                    log.warn("가게 조회 실패. storeId={} 존재하지 않음", storeId);
                    return new RestApiException(ErrorCode.STORE_NOT_FOUND);
                });

        log.info("가게 조회 성공. storeId={}, storeName={}", storeId, store.getStoreName());
				
				...
				
				log.info("메뉴 저장 완료. menuId={}, storeId={}", menu.getId(), storeId);
				
				return MenuResponseDto.from(menu);
		
    }
```

이처럼 @Slf4j 를 붙이면 Logger 객체를 직접 선언할 필요 없이 `log.debug()`, `log.info()` 같은 메서드로 바로 로그를 남길 수 있게 됩니다.

```basic
2025-11-17T00:14:15.568+09:00 DEBUG 48730 --- [nio-8080-exec-2] s.H.domain.Menu.Service.MenuService      : 메뉴 등록 요청 들어옴. storeId=1, dto=server.Heeyoung.domain.Menu.Dto.RequestDto.MenuCreateRequestDto@30f2cbce
Hibernate: 
    select
        s1_0.store_id,
        s1_0.min_price,
        s1_0.store_address,
        s1_0.store_name,
        s1_0.store_phone 
    from
        store s1_0 
    where
        s1_0.store_id=?
2025-11-17T00:14:15.653+09:00  INFO 48730 --- [nio-8080-exec-2] s.H.domain.Menu.Service.MenuService      : 가게 조회 성공. storeId=1, storeName=희영이네 분식
Hibernate: 
    insert 
    into
        menu
        (menu_name, menu_picture, price, store_id) 
    values
        (?, ?, ?, ?)
2025-11-17T00:14:15.689+09:00  INFO 48730 --- [nio-8080-exec-2] s.H.domain.Menu.Service.MenuService      : 메뉴 저장 완료. menuId=7, storeId=1
2025-11-17T00:14:15.730+09:00 DEBUG 48730 --- [nio-8080-exec-2] o.s.w.s.m.m.a.HttpEntityMethodProcessor  : Using 'application/json', given [*/*] and supported [application/json, application/*+json]
2025-11-17T00:14:15.731+09:00 DEBUG 48730 --- [nio-8080-exec-2] o.s.w.s.m.m.a.HttpEntityMethodProcessor  : Writing [server.Heeyoung.domain.Menu.Dto.ResponseDto.MenuResponseDto@35e86de7]
2025-11-17T00:14:15.745+09:00 DEBUG 48730 --- [nio-8080-exec-2] o.s.web.servlet.DispatcherServlet        : Completed 200 OK
```

# 🍀 Swagger는 무엇이며 어떻게 활용할 수 있을까요?

## 1.  Swagger는 무엇일까요?

스웨거는 Web API 를 문서화 하기 위한 도구입니다. API 들이 가지는 명세(Spec) 을 관리하기 쉽도록 시각적인 문서 페이지를 자동으로 생성해주는 역할을 합니다.

## 2.  Spring에 Swagger를 어떻게 적용할 수 있을까요?(with Authorization)

### 1.  build.gradle 파일에 의존성 추가하기

- 아래 의존성을 추가하고 어플리케이션을 실행하면,  [localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) swagger 페이지에 접속할 수 있습니다.

```java
  // swagger
  implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9'
```

### 2.   Swagger Config 작성

```java
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("BearerAuth");

        Components components = new Components()
                .addSecuritySchemes("BearerAuth",
                        new SecurityScheme()
                                .name("BearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"));

        Server devServer = new Server();
        devServer.url("http://localhost:8080");
        devServer.description("Dev Server");

        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(securityRequirement)
                .components(components)
                .servers(List.of(devServer));
    }

    private Info apiInfo() {
        return new Info()
                .title("Heeyoung API Documentation")
                .version("1.0.0")
                .description("Heeyoung Service API Docs");
    }

}
```

- @Configuration 어노테이션을 통해 스프링 빈으로 등록해줍니다.
- openAPI() 객체는 Swagger UI 문서 전체를 정의하는 역할입니다.
- `SecurityRequirement securityRequirement = new SecurityRequirement().addList("BearerAuth");`
    - Swagger 에서 API 테스트를 할 때 자동으로 Authorization 헤더를 붙입니다.
- SecurityScheme 를 설정합니다. JWT 인증 방식을 정의합니다.
    
    → Swagger 가 “Authorization: Bearer XXXXX” 헤더를 지원하게 됩니다.
    
- Swagger 에서 기본으로 사용할 서버 URL 을 지정합니다.
- 최종 openAPI 객체를 빌드합니다.
- Info 객체는 Swagger 의 최상단에 표시되는 API 문서 정보입니다.

## 3.  Controller Layer

Controller Layer 에 Swagger 문서 설정을 쓸 수도 있지만, 그러면 컨트롤러 코드가 너무 길어지기 때문에 API Specification 을 인터페이스로 만들어 추상화합니다.

### 주요 어노테이션

`@Tag`

- API 엔드포인트에 태그를 할당해 관련된 엔드포인트를 그룹화하고, 문서에서 카테고리를 형성하는 데 사용됩니다. 클래스 단에 선언합니다.
    
    ```java
    @Tag(name = "Cart", description = "장바구니 관리 API")
    public interface CartApiSpecification {
    	...
    }
    ```
    

`@Operation` 

- API 엔드포인트의 작업에 대한 설명을 추가하고 세부 정보를 제공합니다.
    
    ```java
        @Operation(
                summary = "장바구니 조회",
                description = "현재 로그인한 사용자의 장바구니 정보를 조회합니다."
        )
    ```
    
    - summary 를 통해 작업을 요약, description 을 통해 작업의 구체적인 설명을 작성할 수 있습니다.

`@ApiResponse` 

- API 응답에 대한 설명과 상태 코드를 정의하는 데 사용합니다.
    
    ```java
        @ApiResponses({
                @ApiResponse(
                        responseCode = "200",
                        description = "✅장바구니 조회 성공",
                        content = @Content(schema = @Schema(implementation = CartResponseDto.class))
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "❌장바구니를 찾을 수 없음 (CART_NOT_FOUND) 또는 사용자를 찾을 수 없음 (USER_NOT_FOUND)",
                        content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                                        examples = @ExampleObject(
                                                value = "{ \"error\": \"CART_NOT_FOUND\", \"message\": \"장바구니를 찾을 수 없습니다.\" }"
                                        )
                        )
                )
        })
    ```
    
    - responseCode 를 통해 상태 코드를 정의하고, description 을 통해 응답에 대해 설명합니다. @ApiResponses 를 통해 여러 개의 ApiResponse 를 등록할 수 있습니다.
    - `@Content` 은 응답 형식(Body) 를 설명합니다.
    - `@Schema` 는 요청과 응답에 사용되는 DTO 기반으로 필드 구조를 자동 생성합니다.
    - `@ExampleObject` 는 실제 JSON 예시를 문서에 추가하는 부분입니다. Swagger 문서에 실제 예씨가 샘플 JSON 으로 표시됩니다. schema 와 달리 “실제 값”을 보여주게 됩니다.

## 4.  DTO

Swagger 는 DTO 클래스에 있는 필드들 + 타입 + 제약 조건 을 읽어서 자동으로 Schema 를 만들어줍니다.

![image.png](%5BApp%20center%5D%207%EC%A3%BC%EC%B0%A8%20-%20Logging%20&%20Swagger/image.png)

또한 아래와 같이 DTO 클래스에 @Schema 어노테이션을 통해 Example 값을 선언해두면 실제 Request Body 입력창까지 채워지게 됩니다.

```java
@Getter
@NoArgsConstructor
public class LoginRequestDto {

    @Schema(description = "로그인 ID", example = "heeyoung09")
    private String loginId;

    @Schema(description = "비밀번호", example = "wlkejf392!")
    private String password;
}

```

![image.png](%5BApp%20center%5D%207%EC%A3%BC%EC%B0%A8%20-%20Logging%20&%20Swagger/image%201.png)

---

# ✅  API 별 로깅 적용하기

## UserAuthService

### `회원가입`

```java
2025-11-18T01:12:55.107+09:00 DEBUG 98090 --- [nio-8080-exec-1] o.s.security.web.FilterChainProxy        : Securing POST /users/sign-up
2025-11-18T01:12:55.114+09:00 DEBUG 98090 --- [nio-8080-exec-1] o.s.s.w.a.AnonymousAuthenticationFilter  : Set SecurityContextHolder to anonymous SecurityContext
2025-11-18T01:12:55.115+09:00 DEBUG 98090 --- [nio-8080-exec-1] o.s.security.web.FilterChainProxy        : Secured POST /users/sign-up
2025-11-18T01:12:55.117+09:00 DEBUG 98090 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : POST "/users/sign-up", parameters={}
2025-11-18T01:12:55.119+09:00 DEBUG 98090 --- [nio-8080-exec-1] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped to server.Heeyoung.domain.User.Controller.UserController#signUpUser(UserSignUpDto)
2025-11-18T01:12:55.197+09:00 DEBUG 98090 --- [nio-8080-exec-1] m.m.a.RequestResponseBodyMethodProcessor : Read "application/json;charset=UTF-8" to [server.Heeyoung.domain.User.dto.request.UserSignUpDto@3f51a0fe]
2025-11-18T01:12:55.251+09:00  INFO 98090 --- [nio-8080-exec-1] s.H.domain.User.Service.UserAuthService  : 회원가입 요청 들어옴. loginId=heeyoung09, email=heeyoung@gmail.com
Hibernate: 
    select
        u1_0.user_id 
    from
        user u1_0 
    where
        u1_0.login_id=? 
    limit
        ?
Hibernate: 
    select
        u1_0.user_id 
    from
        user u1_0 
    where
        u1_0.email=? 
    limit
        ?
Hibernate: 
    insert 
    into
        user
        (address, email, login_id, name, nickname, password, phone_num) 
    values
        (?, ?, ?, ?, ?, ?, ?)
2025-11-18T01:12:55.485+09:00  INFO 98090 --- [nio-8080-exec-1] s.H.domain.User.Service.UserAuthService  : 회원가입 성공. loginId=heeyoung09
2025-11-18T01:12:55.503+09:00 DEBUG 98090 --- [nio-8080-exec-1] o.s.w.s.m.m.a.HttpEntityMethodProcessor  : Using 'text/plain', given [*/*] and supported [text/plain, */*, application/json, application/*+json, application/yaml]
2025-11-18T01:12:55.504+09:00 DEBUG 98090 --- [nio-8080-exec-1] o.s.w.s.m.m.a.HttpEntityMethodProcessor  : Writing ["회원가입에 성공하였습니다."]
2025-11-18T01:12:55.510+09:00 DEBUG 98090 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Completed 200 OK
```

### `로그인`

```java
2025-11-18T01:14:23.139+09:00 DEBUG 98090 --- [nio-8080-exec-5] o.s.security.web.FilterChainProxy        : Securing POST /users/sign-in
2025-11-18T01:14:23.140+09:00 DEBUG 98090 --- [nio-8080-exec-5] o.s.s.w.a.AnonymousAuthenticationFilter  : Set SecurityContextHolder to anonymous SecurityContext
2025-11-18T01:14:23.140+09:00 DEBUG 98090 --- [nio-8080-exec-5] o.s.security.web.FilterChainProxy        : Secured POST /users/sign-in
2025-11-18T01:14:23.140+09:00 DEBUG 98090 --- [nio-8080-exec-5] o.s.web.servlet.DispatcherServlet        : POST "/users/sign-in", parameters={}
2025-11-18T01:14:23.140+09:00 DEBUG 98090 --- [nio-8080-exec-5] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped to server.Heeyoung.domain.User.Controller.UserController#login(UserLoginDto)
2025-11-18T01:14:23.145+09:00 DEBUG 98090 --- [nio-8080-exec-5] m.m.a.RequestResponseBodyMethodProcessor : Read "application/json;charset=UTF-8" to [server.Heeyoung.domain.User.dto.request.UserLoginDto@1771ce82]
2025-11-18T01:14:23.148+09:00  INFO 98090 --- [nio-8080-exec-5] s.H.domain.User.Service.UserAuthService  : 로그인 시도. loginId=heeyoung09
Hibernate: 
    select
        u1_0.user_id,
        u1_0.address,
        u1_0.email,
        u1_0.login_id,
        u1_0.name,
        u1_0.nickname,
        u1_0.password,
        u1_0.phone_num 
    from
        user u1_0 
    where
        u1_0.login_id=?
Hibernate: 
    select
        c1_0.cart_id,
        c1_0.store_id,
        c1_0.user_id 
    from
        cart c1_0 
    where
        c1_0.user_id=?
2025-11-18T01:14:23.436+09:00 DEBUG 98090 --- [nio-8080-exec-5] o.s.s.a.dao.DaoAuthenticationProvider    : Authenticated user
2025-11-18T01:14:23.436+09:00 DEBUG 98090 --- [nio-8080-exec-5] s.H.domain.User.Service.UserAuthService  : 인증 성공. loginId=heeyoung09
2025-11-18T01:14:23.484+09:00  INFO 98090 --- [nio-8080-exec-5] s.H.domain.User.Service.UserAuthService  : 토큰 생성 완료. loginId=heeyoung09
2025-11-18T01:14:23.484+09:00 DEBUG 98090 --- [nio-8080-exec-5] s.H.domain.User.Service.UserAuthService  : RefreshToken 저장 시도. loginId=heeyoung09
Hibernate: 
    select
        rt1_0.id 
    from
        refresh_token rt1_0 
    where
        rt1_0.user_login_id=? 
    limit
        ?
Hibernate: 
    insert 
    into
        refresh_token
        (refresh_token, user_login_id) 
    values
        (?, ?)
2025-11-18T01:14:23.494+09:00  INFO 98090 --- [nio-8080-exec-5] s.H.domain.User.Service.UserAuthService  : RefreshToken 저장 완료. loginId=heeyoung09
2025-11-18T01:14:23.494+09:00  INFO 98090 --- [nio-8080-exec-5] s.H.domain.User.Service.UserAuthService  : 로그인 성공. loginId=heeyoung09
2025-11-18T01:14:23.507+09:00 DEBUG 98090 --- [nio-8080-exec-5] o.s.w.s.m.m.a.HttpEntityMethodProcessor  : Using 'application/json', given [*/*] and supported [application/json, application/*+json, application/yaml]
2025-11-18T01:14:23.507+09:00 DEBUG 98090 --- [nio-8080-exec-5] o.s.w.s.m.m.a.HttpEntityMethodProcessor  : Writing [server.Heeyoung.global.jwt.JwtTokenResponseDto@154ceeab]
2025-11-18T01:14:23.514+09:00 DEBUG 98090 --- [nio-8080-exec-5] o.s.web.servlet.DispatcherServlet        : Completed 200 OK
```

### `AccessToken 재발급`

만료됨…

```java
2025-11-18T01:15:19.931+09:00 DEBUG 98090 --- [nio-8080-exec-6] o.s.security.web.FilterChainProxy        : Securing POST /users/reissue
2025-11-18T01:15:19.931+09:00 DEBUG 98090 --- [nio-8080-exec-6] o.s.s.w.a.AnonymousAuthenticationFilter  : Set SecurityContextHolder to anonymous SecurityContext
2025-11-18T01:15:19.931+09:00 DEBUG 98090 --- [nio-8080-exec-6] o.s.security.web.FilterChainProxy        : Secured POST /users/reissue
2025-11-18T01:15:19.931+09:00 DEBUG 98090 --- [nio-8080-exec-6] o.s.web.servlet.DispatcherServlet        : POST "/users/reissue", parameters={}
2025-11-18T01:15:19.931+09:00 DEBUG 98090 --- [nio-8080-exec-6] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped to server.Heeyoung.domain.User.Controller.UserController#reissueToken(String)
2025-11-18T01:15:19.939+09:00  INFO 98090 --- [nio-8080-exec-6] s.H.domain.User.Service.UserAuthService  : AccessToken 재발급 요청
Hibernate: 
    select
        rt1_0.id,
        rt1_0.refresh_token,
        rt1_0.user_login_id 
    from
        refresh_token rt1_0 
    where
        rt1_0.refresh_token=?
2025-11-18T01:15:19.941+09:00  WARN 98090 --- [nio-8080-exec-6] s.H.domain.User.Service.UserAuthService  : 재발급 실패 → RefreshToken DB에 없음
2025-11-18T01:15:19.948+09:00 DEBUG 98090 --- [nio-8080-exec-6] .m.m.a.ExceptionHandlerExceptionResolver : Using @ExceptionHandler server.Heeyoung.global.exception.GlobalExceptionHandler#handleRestApiException(RestApiException)
2025-11-18T01:15:19.950+09:00 DEBUG 98090 --- [nio-8080-exec-6] o.s.w.s.m.m.a.HttpEntityMethodProcessor  : Using 'application/json', given [*/*] and supported [application/json, application/*+json, application/yaml]
2025-11-18T01:15:19.950+09:00 DEBUG 98090 --- [nio-8080-exec-6] o.s.w.s.m.m.a.HttpEntityMethodProcessor  : Writing [server.Heeyoung.global.exception.ErrorResponseDto@1fa51668]
2025-11-18T01:15:19.951+09:00 DEBUG 98090 --- [nio-8080-exec-6] .m.m.a.ExceptionHandlerExceptionResolver : Resolved [server.Heeyoung.global.exception.RestApiException: 로그인이 만료되었습니다.]
2025-11-18T01:15:19.952+09:00 DEBUG 98090 --- [nio-8080-exec-6] o.s.web.servlet.DispatcherServlet        : Completed 401 UNAUTHORIZED
```

정상 재발급

```java
2025-11-18T01:16:21.384+09:00 DEBUG 98090 --- [nio-8080-exec-8] o.s.security.web.FilterChainProxy        : Securing POST /users/reissue
2025-11-18T01:16:21.384+09:00 DEBUG 98090 --- [nio-8080-exec-8] o.s.s.w.a.AnonymousAuthenticationFilter  : Set SecurityContextHolder to anonymous SecurityContext
2025-11-18T01:16:21.384+09:00 DEBUG 98090 --- [nio-8080-exec-8] o.s.security.web.FilterChainProxy        : Secured POST /users/reissue
2025-11-18T01:16:21.384+09:00 DEBUG 98090 --- [nio-8080-exec-8] o.s.web.servlet.DispatcherServlet        : POST "/users/reissue", parameters={}
2025-11-18T01:16:21.384+09:00 DEBUG 98090 --- [nio-8080-exec-8] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped to server.Heeyoung.domain.User.Controller.UserController#reissueToken(String)
2025-11-18T01:16:21.385+09:00  INFO 98090 --- [nio-8080-exec-8] s.H.domain.User.Service.UserAuthService  : AccessToken 재발급 요청
Hibernate: 
    select
        rt1_0.id,
        rt1_0.refresh_token,
        rt1_0.user_login_id 
    from
        refresh_token rt1_0 
    where
        rt1_0.refresh_token=?
2025-11-18T01:16:21.420+09:00 DEBUG 98090 --- [nio-8080-exec-8] s.H.domain.User.Service.UserAuthService  : RefreshToken 유효. loginId=heeyoung09
Hibernate: 
    select
        u1_0.user_id,
        u1_0.address,
        u1_0.email,
        u1_0.login_id,
        u1_0.name,
        u1_0.nickname,
        u1_0.password,
        u1_0.phone_num 
    from
        user u1_0 
    where
        u1_0.login_id=?
Hibernate: 
    select
        c1_0.cart_id,
        c1_0.store_id,
        c1_0.user_id 
    from
        cart c1_0 
    where
        c1_0.user_id=?
2025-11-18T01:16:21.425+09:00  INFO 98090 --- [nio-8080-exec-8] s.H.domain.User.Service.UserAuthService  : AccessToken 재발급 완료. loginId=heeyoung09
2025-11-18T01:16:21.426+09:00 DEBUG 98090 --- [nio-8080-exec-8] o.s.w.s.m.m.a.HttpEntityMethodProcessor  : Using 'application/json', given [*/*] and supported [application/json, application/*+json, application/yaml]
2025-11-18T01:16:21.426+09:00 DEBUG 98090 --- [nio-8080-exec-8] o.s.w.s.m.m.a.HttpEntityMethodProcessor  : Writing [server.Heeyoung.global.jwt.JwtTokenResponseDto@631f9f1]
2025-11-18T01:16:21.427+09:00 DEBUG 98090 --- [nio-8080-exec-8] o.s.web.servlet.DispatcherServlet        : Completed 200 OK
```

## CartService

### `장바구니 조회`

```java
2025-11-18T01:20:24.700+09:00 DEBUG 98090 --- [nio-8080-exec-2] o.s.security.web.FilterChainProxy        : Securing GET /carts
Hibernate: 
    select
        u1_0.user_id,
        u1_0.address,
        u1_0.email,
        u1_0.login_id,
        u1_0.name,
        u1_0.nickname,
        u1_0.password,
        u1_0.phone_num 
    from
        user u1_0 
    where
        u1_0.login_id=?
Hibernate: 
    select
        c1_0.cart_id,
        c1_0.store_id,
        c1_0.user_id 
    from
        cart c1_0 
    where
        c1_0.user_id=?
2025-11-18T01:20:24.852+09:00 DEBUG 98090 --- [nio-8080-exec-2] o.s.security.web.FilterChainProxy        : Secured GET /carts
2025-11-18T01:20:24.852+09:00 DEBUG 98090 --- [nio-8080-exec-2] o.s.web.servlet.DispatcherServlet        : GET "/carts", parameters={}
2025-11-18T01:20:24.852+09:00 DEBUG 98090 --- [nio-8080-exec-2] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped to server.Heeyoung.domain.Cart.controller.CartController#getCart(UserDetailsImpl)
2025-11-18T01:20:24.875+09:00 DEBUG 98090 --- [nio-8080-exec-2] s.H.domain.Cart.service.CartService      : 장바구니 조회 요청: userId=4
Hibernate: 
    select
        u1_0.user_id,
        u1_0.address,
        c1_0.cart_id,
        c1_0.store_id,
        c1_0.user_id,
        u1_0.email,
        u1_0.login_id,
        u1_0.name,
        u1_0.nickname,
        u1_0.password,
        u1_0.phone_num 
    from
        user u1_0 
    left join
        cart c1_0 
            on u1_0.user_id=c1_0.user_id 
    where
        u1_0.user_id=?
2025-11-18T01:20:24.907+09:00  INFO 98090 --- [nio-8080-exec-2] s.H.domain.Cart.service.CartService      : 유저 조회 성공: userId=4, userName=김희영
Hibernate: 
    select
        c1_0.cart_id,
        cml1_0.cart_id,
        cml1_0.cartmenu_id,
        cml1_0.cart_menu_quantity,
        cml1_0.menu_id,
        c1_0.store_id,
        c1_0.user_id 
    from
        cart c1_0 
    left join
        cart_menu cml1_0 
            on c1_0.cart_id=cml1_0.cart_id 
    where
        c1_0.user_id=?
2025-11-18T01:20:24.969+09:00  INFO 98090 --- [nio-8080-exec-2] s.H.domain.Cart.service.CartService      : 장바구니 조회 성공: cartId=1, menuCount=0
2025-11-18T01:20:24.985+09:00 DEBUG 98090 --- [nio-8080-exec-2] o.s.w.s.m.m.a.HttpEntityMethodProcessor  : Using 'application/json', given [*/*] and supported [application/json, application/*+json, application/yaml]
2025-11-18T01:20:24.986+09:00 DEBUG 98090 --- [nio-8080-exec-2] o.s.w.s.m.m.a.HttpEntityMethodProcessor  : Writing [server.Heeyoung.domain.Cart.dto.response.CartResponseDto@1469fbb9]
2025-11-18T01:20:24.993+09:00 DEBUG 98090 --- [nio-8080-exec-2] o.s.web.servlet.DispatcherServlet        : Completed 200 OK
```

### `장바구니 삭제`

```java
2025-11-18T01:22:06.791+09:00 DEBUG 98090 --- [nio-8080-exec-5] o.s.security.web.FilterChainProxy        : Securing DELETE /carts
Hibernate: 
    select
        u1_0.user_id,
        u1_0.address,
        u1_0.email,
        u1_0.login_id,
        u1_0.name,
        u1_0.nickname,
        u1_0.password,
        u1_0.phone_num 
    from
        user u1_0 
    where
        u1_0.login_id=?
Hibernate: 
    select
        c1_0.cart_id,
        c1_0.store_id,
        c1_0.user_id 
    from
        cart c1_0 
    where
        c1_0.user_id=?
2025-11-18T01:22:06.804+09:00 DEBUG 98090 --- [nio-8080-exec-5] o.s.security.web.FilterChainProxy        : Secured DELETE /carts
2025-11-18T01:22:06.804+09:00 DEBUG 98090 --- [nio-8080-exec-5] o.s.web.servlet.DispatcherServlet        : DELETE "/carts", parameters={}
2025-11-18T01:22:06.804+09:00 DEBUG 98090 --- [nio-8080-exec-5] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped to server.Heeyoung.domain.Cart.controller.CartController#deleteCart(UserDetailsImpl)
2025-11-18T01:22:06.805+09:00 DEBUG 98090 --- [nio-8080-exec-5] s.H.domain.Cart.service.CartService      : 장바구니 삭제 요청: userId=4
Hibernate: 
    select
        u1_0.user_id,
        u1_0.address,
        c1_0.cart_id,
        c1_0.store_id,
        c1_0.user_id,
        u1_0.email,
        u1_0.login_id,
        u1_0.name,
        u1_0.nickname,
        u1_0.password,
        u1_0.phone_num 
    from
        user u1_0 
    left join
        cart c1_0 
            on u1_0.user_id=c1_0.user_id 
    where
        u1_0.user_id=?
2025-11-18T01:22:06.808+09:00  INFO 98090 --- [nio-8080-exec-5] s.H.domain.Cart.service.CartService      : 유저 조회 성공: userId=4, userName=김희영
Hibernate: 
    select
        c1_0.cart_id,
        c1_0.store_id,
        c1_0.user_id 
    from
        cart c1_0 
    left join
        user u1_0 
            on u1_0.user_id=c1_0.user_id 
    where
        u1_0.user_id=?
2025-11-18T01:22:06.811+09:00  INFO 98090 --- [nio-8080-exec-5] s.H.domain.Cart.service.CartService      : 장바구니 조회 성공: cartId=1
2025-11-18T01:22:06.812+09:00 DEBUG 98090 --- [nio-8080-exec-5] s.H.domain.Cart.service.CartService      : 장바구니 메뉴 전체 삭제 시작: cartId=1
Hibernate: 
    select
        cm1_0.cartmenu_id,
        cm1_0.cart_id,
        cm1_0.cart_menu_quantity,
        cm1_0.menu_id 
    from
        cart_menu cm1_0 
    where
        cm1_0.cart_id=?
Hibernate: 
    select
        m1_0.menu_id,
        m1_0.menu_name,
        m1_0.menu_picture,
        m1_0.price,
        m1_0.store_id 
    from
        menu m1_0 
    where
        m1_0.menu_id=?
2025-11-18T01:22:06.830+09:00 DEBUG 98090 --- [nio-8080-exec-5] s.H.domain.Cart.service.CartService      : 유저-카트 연결 해제: userId=4, cartId=1
Hibernate: 
    select
        cml1_0.cart_id,
        cml1_0.cartmenu_id,
        cml1_0.cart_menu_quantity,
        cml1_0.menu_id,
        m1_0.menu_id,
        m1_0.menu_name,
        m1_0.menu_picture,
        m1_0.price,
        m1_0.store_id 
    from
        cart_menu cml1_0 
    left join
        menu m1_0 
            on m1_0.menu_id=cml1_0.menu_id 
    where
        cml1_0.cart_id=?
2025-11-18T01:22:06.837+09:00  INFO 98090 --- [nio-8080-exec-5] s.H.domain.Cart.service.CartService      : 장바구니 삭제 완료: userId=4, cartId=1
Hibernate: 
    delete 
    from
        cart_menu 
    where
        cartmenu_id=?
Hibernate: 
    delete 
    from
        cart 
    where
        cart_id=?
2025-11-18T01:22:06.857+09:00 DEBUG 98090 --- [nio-8080-exec-5] o.s.w.s.m.m.a.HttpEntityMethodProcessor  : Using 'application/json', given [*/*] and supported [application/json, application/*+json, application/yaml]
2025-11-18T01:22:06.857+09:00 DEBUG 98090 --- [nio-8080-exec-5] o.s.w.s.m.m.a.HttpEntityMethodProcessor  : Nothing to write: null body
2025-11-18T01:22:06.858+09:00 DEBUG 98090 --- [nio-8080-exec-5] o.s.web.servlet.DispatcherServlet        : Completed 200 OK
```

## CartMenuService

### `장바구니 메뉴 추가`

```java
2025-11-17T23:49:38.566+09:00 DEBUG 94131 --- [nio-8080-exec-4] o.s.security.web.FilterChainProxy        : Secured POST /carts
2025-11-17T23:49:38.566+09:00 DEBUG 94131 --- [nio-8080-exec-4] o.s.web.servlet.DispatcherServlet        : POST "/carts", parameters={}
2025-11-17T23:49:38.567+09:00 DEBUG 94131 --- [nio-8080-exec-4] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped to server.Heeyoung.domain.Cart.controller.CartController#addMenuToCart(UserDetailsImpl, CartMenuRequestDto)
2025-11-17T23:49:38.571+09:00 DEBUG 94131 --- [nio-8080-exec-4] m.m.a.RequestResponseBodyMethodProcessor : Read "application/json;charset=UTF-8" to [server.Heeyoung.domain.CartMenu.dto.request.CartMenuRequestDto@5bf7a7f9]
2025-11-17T23:49:38.574+09:00 DEBUG 94131 --- [nio-8080-exec-4] s.H.d.CartMenu.Service.CartMenuService   : 장바구니 메뉴 추가 요청: userId=4, dto=server.Heeyoung.domain.CartMenu.dto.request.CartMenuRequestDto@5bf7a7f9
Hibernate: 
    select
        u1_0.user_id,
        u1_0.address,
        c1_0.cart_id,
        c1_0.store_id,
        c1_0.user_id,
        u1_0.email,
        u1_0.login_id,
        u1_0.name,
        u1_0.nickname,
        u1_0.password,
        u1_0.phone_num 
    from
        user u1_0 
    left join
        cart c1_0 
            on u1_0.user_id=c1_0.user_id 
    where
        u1_0.user_id=?
2025-11-17T23:49:38.584+09:00  INFO 94131 --- [nio-8080-exec-4] s.H.d.CartMenu.Service.CartMenuService   : 유저 조회 성공: userId=4, userName=김희영
Hibernate: 
    select
        s1_0.store_id,
        s1_0.min_price,
        s1_0.store_address,
        s1_0.store_name,
        s1_0.store_phone 
    from
        store s1_0 
    where
        s1_0.store_id=?
2025-11-17T23:49:38.585+09:00  INFO 94131 --- [nio-8080-exec-4] s.H.d.CartMenu.Service.CartMenuService   : 가게 조회 성공: storeId=1, storeName=희영이네 분식
Hibernate: 
    select
        c1_0.cart_id,
        c1_0.store_id,
        c1_0.user_id 
    from
        cart c1_0 
    left join
        user u1_0 
            on u1_0.user_id=c1_0.user_id 
    left join
        store s1_0 
            on s1_0.store_id=c1_0.store_id 
    where
        u1_0.user_id=? 
        and s1_0.store_id=?
2025-11-17T23:49:38.590+09:00  INFO 94131 --- [nio-8080-exec-4] s.H.d.CartMenu.Service.CartMenuService   : 기존 장바구니 없음 → 새 장바구니 생성: userId=4, storeId=1
Hibernate: 
    insert 
    into
        cart
        (store_id, user_id) 
    values
        (?, ?)
2025-11-17T23:49:38.593+09:00 DEBUG 94131 --- [nio-8080-exec-4] s.H.d.CartMenu.Service.CartMenuService   : 장바구니 확인 완료: cartId=1
Hibernate: 
    select
        m1_0.menu_id,
        m1_0.menu_name,
        m1_0.menu_picture,
        m1_0.price,
        m1_0.store_id 
    from
        menu m1_0 
    where
        m1_0.menu_id=?
2025-11-17T23:49:38.597+09:00  INFO 94131 --- [nio-8080-exec-4] s.H.d.CartMenu.Service.CartMenuService   : 메뉴 조회 성공: menuId=5, menuName=양념치킨
Hibernate: 
    insert 
    into
        cart_menu
        (cart_id, cart_menu_quantity, menu_id) 
    values
        (?, ?, ?)
2025-11-17T23:49:38.600+09:00  INFO 94131 --- [nio-8080-exec-4] s.H.d.CartMenu.Service.CartMenuService   : 장바구니에 메뉴 추가 완료: cartMenuId=1, cartId=1, quantity=2
2025-11-17T23:49:38.609+09:00 DEBUG 94131 --- [nio-8080-exec-4] o.s.w.s.m.m.a.HttpEntityMethodProcessor  : Using 'application/json', given [*/*] and supported [application/json, application/*+json, application/yaml]
2025-11-17T23:49:38.609+09:00 DEBUG 94131 --- [nio-8080-exec-4] o.s.w.s.m.m.a.HttpEntityMethodProcessor  : Writing [server.Heeyoung.domain.CartMenu.dto.response.CartMenuResponseDto@5f24dd59]
2025-11-17T23:49:38.610+09:00 DEBUG 94131 --- [nio-8080-exec-4] o.s.web.servlet.DispatcherServlet        : Completed 200 OK
```

### `장바구니 메뉴 수량 수정`

```java
2025-11-18T01:24:51.837+09:00 DEBUG 98090 --- [nio-8080-exec-9] o.s.security.web.FilterChainProxy        : Securing PATCH /carts/2?newQuantity=3
Hibernate: 
    select
        u1_0.user_id,
        u1_0.address,
        u1_0.email,
        u1_0.login_id,
        u1_0.name,
        u1_0.nickname,
        u1_0.password,
        u1_0.phone_num 
    from
        user u1_0 
    where
        u1_0.login_id=?
Hibernate: 
    select
        c1_0.cart_id,
        c1_0.store_id,
        c1_0.user_id 
    from
        cart c1_0 
    where
        c1_0.user_id=?
2025-11-18T01:24:51.856+09:00 DEBUG 98090 --- [nio-8080-exec-9] o.s.security.web.FilterChainProxy        : Secured PATCH /carts/2?newQuantity=3
2025-11-18T01:24:51.859+09:00 DEBUG 98090 --- [nio-8080-exec-9] o.s.web.servlet.DispatcherServlet        : PATCH "/carts/2?newQuantity=3", parameters={masked}
2025-11-18T01:24:51.859+09:00 DEBUG 98090 --- [nio-8080-exec-9] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped to server.Heeyoung.domain.Cart.controller.CartController#updateCartMenuQuantity(Long, UserDetailsImpl, Long)
2025-11-18T01:24:51.886+09:00 DEBUG 98090 --- [nio-8080-exec-9] s.H.d.CartMenu.Service.CartMenuService   : 장바구니 메뉴 수량 수정 요청: userId=4, cartMenuId=2, newQuantity=3
Hibernate: 
    select
        cm1_0.cartmenu_id,
        cm1_0.cart_id,
        c1_0.cart_id,
        c1_0.store_id,
        c1_0.user_id,
        cm1_0.cart_menu_quantity,
        cm1_0.menu_id,
        m1_0.menu_id,
        m1_0.menu_name,
        m1_0.menu_picture,
        m1_0.price,
        m1_0.store_id 
    from
        cart_menu cm1_0 
    join
        cart c1_0 
            on c1_0.cart_id=cm1_0.cart_id 
    join
        menu m1_0 
            on m1_0.menu_id=cm1_0.menu_id 
    where
        cm1_0.cartmenu_id=?
2025-11-18T01:24:51.894+09:00  INFO 98090 --- [nio-8080-exec-9] s.H.d.CartMenu.Service.CartMenuService   : 장바구니 메뉴 수량 수정 완료: cartMenuId=2, newQuantity=3
Hibernate: 
    update
        cart_menu 
    set
        cart_id=?,
        cart_menu_quantity=?,
        menu_id=? 
    where
        cartmenu_id=?
2025-11-18T01:24:51.934+09:00 DEBUG 98090 --- [nio-8080-exec-9] o.s.w.s.m.m.a.HttpEntityMethodProcessor  : Using 'application/json', given [*/*] and supported [application/json, application/*+json, application/yaml]
2025-11-18T01:24:51.935+09:00 DEBUG 98090 --- [nio-8080-exec-9] o.s.w.s.m.m.a.HttpEntityMethodProcessor  : Writing [server.Heeyoung.domain.CartMenu.dto.response.CartMenuResponseDto@45b5cedd]
2025-11-18T01:24:51.935+09:00 DEBUG 98090 --- [nio-8080-exec-9] o.s.web.servlet.DispatcherServlet        : Completed 200 OK
```

### `장바구니 메뉴 삭제`

```java
2025-11-18T01:27:12.136+09:00 DEBUG 98090 --- [nio-8080-exec-2] o.s.security.web.FilterChainProxy        : Securing DELETE /carts/2
Hibernate: 
    select
        u1_0.user_id,
        u1_0.address,
        u1_0.email,
        u1_0.login_id,
        u1_0.name,
        u1_0.nickname,
        u1_0.password,
        u1_0.phone_num 
    from
        user u1_0 
    where
        u1_0.login_id=?
Hibernate: 
    select
        c1_0.cart_id,
        c1_0.store_id,
        c1_0.user_id 
    from
        cart c1_0 
    where
        c1_0.user_id=?
2025-11-18T01:27:12.149+09:00 DEBUG 98090 --- [nio-8080-exec-2] o.s.security.web.FilterChainProxy        : Secured DELETE /carts/2
2025-11-18T01:27:12.149+09:00 DEBUG 98090 --- [nio-8080-exec-2] o.s.web.servlet.DispatcherServlet        : DELETE "/carts/2", parameters={}
2025-11-18T01:27:12.149+09:00 DEBUG 98090 --- [nio-8080-exec-2] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped to server.Heeyoung.domain.Cart.controller.CartController#deleteCartMenu(Long, UserDetailsImpl)
2025-11-18T01:27:12.153+09:00 DEBUG 98090 --- [nio-8080-exec-2] s.H.d.CartMenu.Service.CartMenuService   : 장바구니 메뉴 삭제 요청: userId=4, cartMenuId=2
Hibernate: 
    select
        cm1_0.cartmenu_id,
        cm1_0.cart_id,
        c1_0.cart_id,
        c1_0.store_id,
        c1_0.user_id,
        cm1_0.cart_menu_quantity,
        cm1_0.menu_id,
        m1_0.menu_id,
        m1_0.menu_name,
        m1_0.menu_picture,
        m1_0.price,
        m1_0.store_id 
    from
        cart_menu cm1_0 
    join
        cart c1_0 
            on c1_0.cart_id=cm1_0.cart_id 
    join
        menu m1_0 
            on m1_0.menu_id=cm1_0.menu_id 
    where
        cm1_0.cartmenu_id=?
2025-11-18T01:27:12.161+09:00  INFO 98090 --- [nio-8080-exec-2] s.H.d.CartMenu.Service.CartMenuService   : 장바구니 메뉴 삭제 완료: cartMenuId=2, userId=4
Hibernate: 
    delete 
    from
        cart_menu 
    where
        cartmenu_id=?
2025-11-18T01:27:12.169+09:00 DEBUG 98090 --- [nio-8080-exec-2] o.s.w.s.m.m.a.HttpEntityMethodProcessor  : Using 'application/json', given [*/*] and supported [application/json, application/*+json, application/yaml]
2025-11-18T01:27:12.169+09:00 DEBUG 98090 --- [nio-8080-exec-2] o.s.w.s.m.m.a.HttpEntityMethodProcessor  : Nothing to write: null body
2025-11-18T01:27:12.170+09:00 DEBUG 98090 --- [nio-8080-exec-2] o.s.web.servlet.DispatcherServlet        : Completed 200 OK
```

## MenuService

### `메뉴 등록`

```java
2025-11-18T01:29:03.595+09:00 DEBUG 98090 --- [nio-8080-exec-5] o.s.security.web.FilterChainProxy        : Securing POST /stores/1/menus
2025-11-18T01:29:03.596+09:00 DEBUG 98090 --- [nio-8080-exec-5] o.s.s.w.a.AnonymousAuthenticationFilter  : Set SecurityContextHolder to anonymous SecurityContext
2025-11-18T01:29:03.597+09:00 DEBUG 98090 --- [nio-8080-exec-5] o.s.security.web.FilterChainProxy        : Secured POST /stores/1/menus
2025-11-18T01:29:03.597+09:00 DEBUG 98090 --- [nio-8080-exec-5] o.s.web.servlet.DispatcherServlet        : POST "/stores/1/menus", parameters={}
2025-11-18T01:29:03.598+09:00 DEBUG 98090 --- [nio-8080-exec-5] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped to server.Heeyoung.domain.Menu.Controller.MenuController#createMenu(Long, MenuCreateRequestDto)
2025-11-18T01:29:03.624+09:00 DEBUG 98090 --- [nio-8080-exec-5] m.m.a.RequestResponseBodyMethodProcessor : Read "application/json;charset=UTF-8" to [server.Heeyoung.domain.Menu.Dto.RequestDto.MenuCreateRequestDto@4dfa9594]
2025-11-18T01:29:03.628+09:00 DEBUG 98090 --- [nio-8080-exec-5] s.H.domain.Menu.Service.MenuService      : 메뉴 등록 요청 들어옴. storeId=1, dto=server.Heeyoung.domain.Menu.Dto.RequestDto.MenuCreateRequestDto@4dfa9594
Hibernate: 
    select
        s1_0.store_id,
        s1_0.min_price,
        s1_0.store_address,
        s1_0.store_name,
        s1_0.store_phone 
    from
        store s1_0 
    where
        s1_0.store_id=?
2025-11-18T01:29:03.634+09:00  INFO 98090 --- [nio-8080-exec-5] s.H.domain.Menu.Service.MenuService      : 가게 조회 성공. storeId=1, storeName=희영이네 분식
Hibernate: 
    insert 
    into
        menu
        (menu_name, menu_picture, price, store_id) 
    values
        (?, ?, ?, ?)
2025-11-18T01:29:03.648+09:00  INFO 98090 --- [nio-8080-exec-5] s.H.domain.Menu.Service.MenuService      : 메뉴 저장 완료. menuId=7, storeId=1
2025-11-18T01:29:03.662+09:00 DEBUG 98090 --- [nio-8080-exec-5] o.s.w.s.m.m.a.HttpEntityMethodProcessor  : Using 'application/json', given [*/*] and supported [application/json, application/*+json, application/yaml]
2025-11-18T01:29:03.663+09:00 DEBUG 98090 --- [nio-8080-exec-5] o.s.w.s.m.m.a.HttpEntityMethodProcessor  : Writing [server.Heeyoung.domain.Menu.Dto.ResponseDto.MenuResponseDto@25ace009]
2025-11-18T01:29:03.664+09:00 DEBUG 98090 --- [nio-8080-exec-5] o.s.web.servlet.DispatcherServlet        : Completed 200 OK
```