# spring-cloud-aws-starter-dynamodb

> novo starter do spring cloud, provendo autoconfiguração do client do dynamodb de forma simples, abstração de métodos através de uma interface template para utilização nos services e facilidade de configuração na utilização do DAX no client do Dynamodb através de propriedades de configuração na aplicação.

## sumário
1. [Standard autoconfiguração ](#solução-starter-dynamodb)
   1. [Autoconfiguração no client do DynamoDB](#autoconfiguração-no-client-do-dynamodb)
   2. [Abstração dos métodos para operações no Dynamo](#abstração-dos-métodos-para-operações-no-dynamo)
   3. [Como utilizar](#como-utilizar)
2. 


## solução starter-dynamodb

### autoconfiguração no client do DynamoDB
Com o starter-dynamodb não há necessidade de declarar beans do `DynamoDbClient` e injetar em `DynamoDbEnhancedClient` para fazer a utilização dos métodos de tabela.

Forma manual de configurar o client com a sdk do dynamo v2:
```java
   @Bean
    public DynamoDbClient  dynamoDbClient() {
            return DynamoDbClient.builder()
            .credentialsProvider(STATIC_CREDENTIALS)
            .endpointOverride(URI_LOCALSTACK)
            .region(Region.SA_EAST_1)
            .build();
            }
    
    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient client) {
            return DynamoDbEnhancedClient.builder().dynamoDbClient(client).build();
            }
```
com o starter isso é feito de forma automática na classe de configuração `DynamoDbAutoConfiguration`

```java
	@Conditional(MissingDaxUrlCondition.class)
	@Configuration(proxyBeanMethods = false)
	static class StandardDynamoDbClient {

		@ConditionalOnMissingBean
		@Bean
		public DynamoDbClient dynamoDbClient(AwsClientBuilderConfigurer awsClientBuilderConfigurer,
				ObjectProvider<AwsClientCustomizer<DynamoDbClientBuilder>> configurer, DynamoDbProperties properties) {
			return awsClientBuilderConfigurer
					.configure(DynamoDbClient.builder(), properties, configurer.getIfAvailable()).build();
		}

	}

	@ConditionalOnMissingBean
	@Bean
	public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
		return DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build();
	}

	@ConditionalOnMissingBean(DynamoDbOperations.class)
	@Bean
	public DynamoDbTemplate dynamoDBTemplate(DynamoDbEnhancedClient dynamoDbEnhancedClient,
			Optional<DynamoDbTableSchemaResolver> tableSchemaResolver,
			Optional<DynamoDbTableNameResolver> tableNameResolver) {
		DynamoDbTableSchemaResolver tableSchemaRes = tableSchemaResolver
				.orElseGet(DefaultDynamoDbTableSchemaResolver::new);
		DynamoDbTableNameResolver tableNameRes = tableNameResolver.orElseGet(DefaultDynamoDbTableNameResolver::new);
		return new DynamoDbTemplate(dynamoDbEnhancedClient, tableSchemaRes, tableNameRes);
	}
```

Conta também com a propriedade `spring.cloud.aws.dynamodb.endpoint` para sobrescrever o endpoint do client, de forma fácil a utlização com **LocalStack** por exemplo.

Lista de propriedades do starter-dynamodb:

| nome |                   descrição                   | valor default |
| :---:   |:---------------------------------------------:|:-------------:|
| spring.cloud.aws.dynamodb.enabled |         habilita a auto configuração          |     true      |
| spring.cloud.aws.dynamodb.endpoint | configura o endpoint usado em DynamodB Client |               |
| spring.cloud.aws.dynamodb.region |  configura a region usado em DynamodB Client  |               |

### Abstração dos métodos para operações no Dynamo

Através da interface `DynamoDbOperations`é possível utilizar os métodos para utilizar os recursos do Dynamo para requisitar dados e atualizar dados, isso é possível por causa da auto configuração do client

Tabela dos métodos disponível através da interface:

| _`Nome do método`_ |                _`parametros`_                |                       _`descrição`_                        |
|:--------------:|:----------------------------------------:|:------------------------------------------------------:|
|    save     |                 T entity                 |       salva uma entidade na tabbela do DynamoDB.       |
|    update     |                 T entity                 |      atualiza uma entidade na tabela do DynamoDB.      |
|    delete     | Key key, Class<?> clazz<br/> OU <br/>T entity |       Deleta um record para a entidade recebida.       |
|    load     |  Key key, Class<T> clazz                    |  carrega a entidade com a perspectiva chave passada.   |
|    scanAll     |                   Class<T> clazz                    | realiza o scan de toda a tabela, incluindo seus items. |
|    query     |QueryEnhancedRequest queryEnhancedRequest, Class<T> clazz|        query o data da entidade personalizavél         |
|    scan     |ScanEnhancedRequest scanEnhancedRequest, Class<T> clazz|      scan personalizavel para items ou expression      |

Os métodos injetam em DynamoDbTable através do método em todas as chamadas de forma unitária.
```java
        private <T> DynamoDbTable<T> prepareTable(T entity) {
        String tableName = dynamoDbTableNameResolver.resolve(entity.getClass());
        return dynamoDbEnhancedClient.table(tableName,
        dynamoDbTableSchemaResolver.resolve(entity.getClass(), tableName));
        }
    
        private <T> DynamoDbTable<T> prepareTable(Class<T> clazz) {
        String tableName = dynamoDbTableNameResolver.resolve(clazz);
        return dynamoDbEnhancedClient
        .table(tableName, dynamoDbTableSchemaResolver.resolve(clazz, tableName));
        }
```

### Como utilizar

Exemplo de utilização standart sem a utilização do dax, primeiro adicione a dependência `spring-cloud-aws-starter-s3` no pom ou gradle.
```xml
		<dependency>
			<groupId>io.awspring.cloud</groupId>
			<artifactId>spring-cloud-aws-starter-dynamodb</artifactId>
                        <version>3.0.0-RC1</version>
		</dependency>
```

```groovy
implementation 'io.awspring.cloud:spring-cloud-aws-starter-dynamodb:3.0.0-RC1'
```

Para realizar as operações no banco do Dynamodb precisamos descrever o Bean da nossa entidade conforme abaixo.

```java
@DynamoDbBean
public class Customer {
    private String id;
    private String name;
    private String age;

    public Customer(String name, String age) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.age = age;
    }

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }
    
    //getters and setters
```

> **_NOTE:_**  a entidade precisa estar anotada com @DynamodbBean. Visto que a auto configuração do TableSchema estar apontando para o um Bean.
> > **_WARNING:_**  A autoconfiguração do client é incompativel com a seguinte anotação Dynamo, @DynamoDbImmutable.




