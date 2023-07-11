#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <ESP8266HTTPClient.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include "DHT.h"  

// WLAN,API Server & MQTT Broker Info
const char* ssid = "Bao";
const char* password = "0938003938";
const char* MQTTbroker = "192.168.1.10";
const int MQTTport = 1883;
// Create DHT22 instance
#define DHTPIN D4
const int DHTTYPE = DHT11; 
DHT dht(DHTPIN, DHTTYPE);
// Vibration sensor GPIO
#define VIBRATION_SENSOR_PIN D2
// Temp & Hum variables: store temp & hum values read from DHT sensor
float temp;
float hum;
// Vibration variable: store vibration value read from SW420 sensor
long vib;

// For interval checking
unsigned long lastMsg = 0;
// Variables for MQTT Message
WiFiClient wificlientMQTT;
PubSubClient client(wificlientMQTT);
const char* id = "11111";
const char* name = "Wemos D1 R2 #1";
String ip;
//LWT message.
char LWTmsg[100];
int motionSensorval = 0;

void set_mqtt_disconnect_message()
{
    StaticJsonDocument<64> doc;

    doc["id"] = id;
    doc["status"] = false;

    serializeJson(doc, LWTmsg);
    doc.clear();
}

void sending_mqtt_connect_message()
{
    const size_t capacity = JSON_OBJECT_SIZE(4) + 50;
    char buffer[capacity];
    DynamicJsonDocument doc(1024);
    doc["id"] = id;
    doc["name"] = name;
    doc["ip"] = ip;
    doc["status"] = true; // Wemos connection to MQTT Broker status

    serializeJson(doc, buffer);
    Serial.println(buffer);
    client.publish("/wemos/connection", buffer);

    doc.clear();    
}

void sending_mqtt_sensor_value()
{
    const size_t capacity = JSON_OBJECT_SIZE(2) + 50;
    char buffer[capacity];
    DynamicJsonDocument doc(1024);
    doc["temperature"] = temp;
    doc["humidity"] = hum;
    doc["vibration"] = vib;

    serializeJson(doc, buffer);
    Serial.println(buffer);
    client.publish("/wemos/sensor", buffer);

    doc.clear();    
}

void reconnect() {
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    String clientId = "ESP8266Client-";
    clientId += String(random(0xffff), HEX);
    if (client.connect(clientId.c_str(),"/wemos/connection", 0, false, LWTmsg)) {
      Serial.println("connected");
      // Send initial connection info
      sending_mqtt_connect_message();
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");
      delay(5000);
    }
  }
  Serial.println(""); Serial.println("--------------------------------------------"); Serial.println("");
}

void setup() 
{
  Serial.begin(9600);
  delay(100);

  // Init DHT sensor
  dht.begin();

  // Set pin mode for SW420 GPIO
  pinMode(VIBRATION_SENSOR_PIN, INPUT);

  // Connect to Wi-Fi network
  WiFi.begin(ssid, password);
  Serial.println("Connecting to Wi-Fi");

  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("Connected to Wi-Fi");
  Serial.println("IP address: ");
  ip = WiFi.localIP().toString();
  Serial.println(WiFi.localIP());
  Serial.println(""); Serial.println("--------------------------------------------"); Serial.println("");

  // Set MQTT Broker connect info 
  client.setServer(MQTTbroker, MQTTport);

  // Set LWT message for the event of disconnection
  set_mqtt_disconnect_message();
}

long vibration()
{
  long measurement = pulseIn(VIBRATION_SENSOR_PIN, HIGH);
  return measurement;
}

void loop() 
{
  // Connect to MQTT broker
  if (!client.connected()) {
    reconnect();
  }

  client.loop();

  // Collect sensor data and publish them to a topic ( 1s interval)
  unsigned long now = millis();
  if (now - lastMsg >= 1000) {
    lastMsg = now;

    temp = dht.readTemperature(); 
    hum = dht.readHumidity();
    vib = vibration();

    Serial.print("Temperature: "); Serial.println(temp);
    Serial.print("Humidity: "); Serial.println(hum);
    Serial.print("Vibration: "); Serial.println(vib);
    Serial.println();

    sending_mqtt_sensor_value();
  }
}