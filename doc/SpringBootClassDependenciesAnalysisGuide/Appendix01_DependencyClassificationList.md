# 依存関係分類一覧

|No|カテゴリ|サブカテゴリ|説明|代表例(Java記法)|解析シグナル|対象スコープ|依存フレームワーク|
|----|----|----|----|----|----|----|----|
|1|型依存|継承(extends)|クラスが親クラスを継承する|class OrderService extends BaseService|extends句/継承階層|クラス|Java|
|2|型依存|実装(implements)|クラスがインタフェースを実装する|class UserService implements UserDetailsService|implements句|クラス|Java|
|3|型依存|型参照(import/qualified)|他クラスを型として参照する|import com.example.User; User u;|import文/修飾名参照|クラス/メソッド/フィールド|Java|
|4|型依存|ジェネリクス型参照|ジェネリック実引数による型依存|List<User>, Map<Long, Order>|ParameterizedType/TypeArgument|メソッド/フィールド|Java|
|5|型依存|例外型依存|throws/try-catchでの例外依存|void f() throws CustomException|throws句/catch節|メソッド|Java|
|6|メソッド依存|メソッド呼び出し|他クラスのメソッド呼び出し|orderRepo.findById(id)|MethodInvocation/レシーバ解決|メソッド|Java|
|7|メソッド依存|戻り値型依存|メソッド戻り値の型依存|User findOne()|メソッド宣言のReturnType|メソッド|Java|
|8|メソッド依存|引数型依存|引数として他型を要求|save(User dto)|メソッド宣言のParameterType|メソッド|Java|
|9|メソッド依存|静的メソッド依存|static呼び出しによる依存|Objects.requireNonNull(x)|StaticMethodInvocation|メソッド|Java|
|10|フィールド依存|コンポジション(保持)|フィールドとして他型を保持|private Address address;|Field宣言の型|フィールド/クラス|Java|
|11|フィールド依存|集合保持|コレクションで他型を保持|List<Order> orders;|FieldのParameterizedType|フィールド/クラス|Java|
|12|フィールド依存|定数参照|他クラスの定数参照|Status.ACTIVE|QualifiedName/static final|メソッド/フィールド|Java|
|13|DI依存|コンストラクタDI|コンストラクタで依存注入|UserService(UserRepository repo)|@Autowired/単一コンストラクタ|クラス|Spring Core|
|14|DI依存|フィールドDI|フィールドに直接注入|@Autowired UserRepository repo;|@Autowired on Field|フィールド/クラス|Spring Core|
|15|DI依存|Setter DI|セッターで依存注入|@Autowired setRepo(UserRepository r)|@Autowired on Method|メソッド/クラス|Spring Core|
|16|DI依存|@Bean提供|@Configuration内でBean生成|@Bean PasswordEncoder encoder()|@Bean/@Configuration|メソッド/クラス|Spring Core|
|17|Spring依存|MVCコントローラ|HTTPエンドポイントの定義|@RestController @GetMapping("/users")|@Controller/@RequestMapping群|クラス/メソッド|Spring MVC|
|18|Spring依存|サービス層|ビジネスロジックのBean|@Service class UserService {}|@Service注釈|クラス|Spring Core|
|19|Spring依存|リポジトリ層|データアクセス層のBean|interface UserRepo extends JpaRepository<User,Long>|@Repository/JpaRepository継承|クラス|Spring Data|
|20|Repository依存|JPAエンティティ|永続化対象ドメインの定義|@Entity class User { @Id Long id; }|@Entity/@Id|クラス/フィールド|Spring Data|
|21|Repository依存|クエリメソッド|命名規約での問い合わせ依存|findByEmail(String email)|メソッド名解析|メソッド|Spring Data|
|22|DTO/Entity依存|DTO|入出力用データ構造|class UserDto { ... }|POJO/Record検出|クラス|Java|
|23|DTO/Entity依存|マッパー|DTO?Entity変換|@Mapper interface UserMapper { ... }|@Mapper/@Mapping|クラス/メソッド|Java|
|24|設定値依存|@Value注入|プロパティ値の注入|@Value("${app.mode}") String mode;|@Value/Placeholder|フィールド/コンストラクタ|Spring Core|
|25|設定値依存|構成プロパティ|型安全な設定バインド|@ConfigurationProperties(prefix="app")|@ConfigurationProperties|クラス/フィールド|Spring Boot|
|26|イベント依存|アプリイベント購読|アプリ内イベントの購読|@EventListener(UserCreatedEvent.class)|@EventListener/ApplicationEvent|メソッド|Spring Core|
|27|外部サービス依存|HTTPクライアント|外部REST/API呼び出し|WebClient.get(...)/@FeignClient|WebClient/Feign注釈|メソッド/クラス|Spring Core|
|28|外部サービス依存|メッセージング|キュー/トピック依存|@KafkaListener / @RabbitListener|メッセージング注釈|メソッド/クラス|Spring Core|
|29|AOP依存|トランザクション|メソッド境界のTx管理|@Transactional|@Transactional/TxManager|メソッド/クラス|Spring Core|
|30|AOP依存|横断的関心事|ロギング/監査/リトライ等のAOP|@Aspect @Around("execution(*..*)")|AspectJ注釈/Pointcut|メソッド/クラス|Spring Core|
|31|Security(設定)|SecurityFilterChain|HTTPセキュリティ設定の中心|@Bean SecurityFilterChain security(HttpSecurity)|@Bean/HttpSecurity|メソッド/クラス|Spring Security|
|32|Security(設定)|HttpSecurityルール|認可/CSRF/セッション等の設定|http.authorizeHttpRequests(...)|authorizeHttpRequests/csrf/session|メソッド|Spring Security|
|33|Security(認証モデル)|UserDetails|認証対象ユーザの表現|class MyUserDetails implements UserDetails|UserDetails実装|クラス|Spring Security|
|34|Security(認証モデル)|UserDetailsService|ユーザ情報の読込|loadUserByUsername(String name)|UserDetailsService実装|クラス/メソッド|Spring Security|
|35|Security(認証モデル)|PasswordEncoder|パスワード検証|new BCryptPasswordEncoder()|PasswordEncoder Bean参照|メソッド/クラス|Spring Security|
|36|Security(認証モデル)|AuthenticationManager|認証処理の起点/委譲先|authManager.authenticate(token)|AuthenticationManager参照|メソッド|Spring Security|
|37|Security(認証モデル)|AuthenticationProvider|認証方式ごとの実装|DaoAuthenticationProvider / custom JwtProvider|Provider Bean|クラス/メソッド|Spring Security|
|38|Security(フィルタ)|OncePerRequestFilter|リクエスト毎のカスタム検査|class JwtFilter extends OncePerRequestFilter|継承/doFilterInternal|クラス/メソッド|Spring Security|
|39|Security(認可)|メソッドセキュリティ|メソッド単位の認可|@PreAuthorize("hasRole('ADMIN')")|@EnableMethodSecurity/@Pre/PostAuthorize|メソッド/クラス|Spring Security|
|40|Security(認可)|ロール/権限|権限集合による制御|ROLE_ADMIN / SCOPE_read|GrantedAuthority/Authority名|フィールド/メソッド|Spring Security|
|41|Security(セッション/Context)|SecurityContext|認証情報の保存/参照|SecurityContextHolder.getContext()|SecurityContextHolder参照|メソッド|Spring Security|
|42|Security(セッション/Context)|Session管理|状態管理と戦略設定|http.sessionManagement()|sessionCreationPolicy/MaxSessions|メソッド|Spring Security|
|43|Security(JWT/Token)|トークン抽出|Header等からJWT抽出/前処理|request.getHeader("Authorization")|Bearerヘッダ検出|メソッド/フィルタ|Spring Security|
|44|Security(JWT/Token)|署名/検証|鍵での署名検証/有効期限確認|Nimbus JWT: JWSVerifier|JWTライブラリ呼出|メソッド/クラス|Java|
|45|Security(JWT/Token)|クレーム→権限マッピング|claimsからGrantedAuthority生成|roles -> new SimpleGrantedAuthority(...)|claims解析/権限マップ|メソッド|Spring Security|
|46|Security(エンドポイント)|ログイン/ログアウト|認証系エンドポイントの保護/設定|/login, /logout, oauth2Login()|requestMatchers/formLogin/oauth2Login|メソッド|Spring Security|
|47|ライブラリ依存|Lombok|ボイラープレート削減注釈|@Getter @Setter @Builder|lombok.*注釈検出|クラス/フィールド|Java|
|48|ライブラリ依存|Jackson|JSONのシリアライズ/デシリアライズ|@JsonProperty / new ObjectMapper()|com.fasterxml.jackson.*参照|クラス/フィールド/メソッド|Java|
|49|アーキ依存|Controller→Service|層間の典型依存|UserController -> UserService|コンストラクタDI/呼び出しグラフ|クラス/メソッド|Spring MVC|
|50|アーキ依存|Service→Repository|ビジネス→永続化|UserService -> UserRepository|DI+メソッド呼出|クラス/メソッド|Spring Data|
|51|アーキ依存|Repository→Entity|永続化対象への依存|UserRepository -> User|JpaRepository<T,ID>のT|クラス|Spring Data|
|52|Web特有|パス/パラメータ依存|URL/PathVariable/RequestParam依存|@GetMapping("/users/{id}")|アノテーション属性解析|メソッド|Spring MVC|
|53|設定/プロファイル|プロファイル条件|Bean定義の条件/切替|@Profile("prod")/@Conditional|@Profile/@Conditional|クラス/メソッド|Spring Core|
|54|構成/起動|オートコンフィグ|Spring Bootの自動構成依存|spring-boot-starter-security など|AutoConfiguration/条件付きBean|設定/ビルド|Spring Boot|
|55|ビルド依存|Maven/Gradle|依存関係とスコープの宣言|compileOnly/runtimeOnly|pom.xml/build.gradle宣言|ビルド|Java|
|56|テスト依存|テストスライス|層別テストコンテキスト|@WebMvcTest @DataJpaTest|テスト注釈/スライス起動|テスト|Spring Boot|
|57|国際化/バリデーション|Bean Validation|入力検証/Jakarta Validation|@Valid @NotNull Validator|jakarta.validation.*参照|メソッド/フィールド|Java|
|58|観測性|ログ/メトリクス|メトリクス/トレースの収集/公開|@Timed / Micrometer|Actuator/Micrometer連携|メソッド/クラス|Spring Boot|
|59|セキュリティ補助|CORS/CSRF|ブラウザ保護設定|http.cors() / http.csrf()|HttpSecurityの設定項目|メソッド|Spring Security|
|60|設計指標|循環依存|パッケージ/クラスの循環参照|A -> B -> A|依存グラフの閉路検出|パッケージ/クラス|Java|
