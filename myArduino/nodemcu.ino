#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <DHT22.h>

// WiFi 설정
const char* ssid = "JHSHOME";
const char* password = "jhs93650914";

// DHT22 설정
#define pinDATA D5 // DHT22 데이터 핀
DHT22 dht22(pinDATA);

// 서버 주소
const char* serverAddress = "http://172.30.1.2:8080/data"; // 여기에 스프링 부트 서버 주소를 넣으세요.

WiFiClient client;

void setup() {
  Serial.begin(115200);
  
  // WiFi 연결
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connecting to WiFi...");
  }
  Serial.println("Connected to WiFi");
}

void loop() {
  // 온습도 데이터 가져오기
  float humidity = dht22.getHumidity();
  float temperature = dht22.getTemperature();
  
  // JSON 데이터 생성
  String jsonData = "{\"temperature\":" + String(temperature) + ",\"humidity\":" + String(humidity) + "}";
  
  // HTTPClient 초기화
  HTTPClient http;
  
  // 서버에 POST 요청 보내기
  http.begin(client, serverAddress); // 수정된 부분
  http.addHeader("Content-Type", "application/json");
  int httpResponseCode = http.POST(jsonData);
  
  // 응답 출력
  if (httpResponseCode > 0) {
    String response = http.getString();
    Serial.println("HTTP Response code: " + String(httpResponseCode));
    Serial.println("Response: " + response);
  } else {
    Serial.println("Error code: " + String(httpResponseCode));
  }

  // 연결 종료
  http.end();
  
  // 5초 대기
  delay(60000);
}