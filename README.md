# CrptApi Java Client

## Описание технического задания

Разработать клиент на **Java** для взаимодействия с **API Честного знака**. Клиент должен обеспечивать контроль над 
частотой запросов к **API**, не допуская превышения установленных лимитов. Это позволит эффективно использовать **API**, 
не перегружая его избыточными запросами.

## CrptApi устроен
Клиент использует `Semaphore` для управления количеством одновременно выполняемых запросов. Когда лимит запросов достигнут, 
новые запросы блокируются и ожидают, пока не появится свободное разрешение для их выполнения. Таким образом, клиент
гарантирует, что количество активных запросов никогда не превысит установленный лимит.

```java
    public CrptApi(TimeUnit timeUnit, int requestLimit, int intervalTime, String url) {
        this.semaphore = new Semaphore(requestLimit);
        this.url = url;
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        scheduledExecutorService.scheduleAtFixedRate(() -> semaphore.release(0),
                INITIAL_DELAY, intervalTime, timeUnit);
    }
```

Для использования **CrptApi** необходимо создать экземляр класса с указанием:
* Промежуток времени, например: `TimeUnit.SECONDS`
* Максимальное количество запросов в промежутке времени
* Единицу интервала времени
* URL адрес

пример создания экземпляра класса **CrptApi**: 

```java
private static final String ISMP_CRPT_RU = "https://ismp.crpt.ru/api/v3/lk/documents/create";
private static final int REQUEST_LIMIT = 5;
private static final int INTERVAL_TIME = 10;
...........
        CrptApi api = new CrptApi(TimeUnit.SECONDS, REQUEST_LIMIT, INTERVAL_TIME, ISMP_CRPT_RU);
```

## Создание документа
Для создания документа используется метод `createDocument(Document document, String signature)`
В параметры которого передаются:
* `Document` класс который описан в онсновном классе **CrptApi**
* `signature` подпись документа

В данном методе отправляется документ к `API`, в методе установлено ограничение 
доступа запросов через `Semaphore`. 

## Используемые зависимости
* gson
* lombok
