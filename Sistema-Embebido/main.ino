#include "hx711.h"
#include <SoftwareSerial.h> //Librería que permite establecer comunicación serie en otros pins
#include <OneWire.h>                
#include <DallasTemperature.h>

Hx711 scale(A1, A0); // Hx711.DOUT - pin #A1 Hx711.SCK - pin #A0
SoftwareSerial BT(7,8); // 7 RX, 8 TX.  Aquí conectamos los pins RXD,TDX del módulo Bluetooth.
OneWire ourWire(2); //Se establece el pin 2  como bus OneWire
DallasTemperature sensorTemp(&ourWire); //Se declara una variable u objeto para nuestro sensor
int estaon = HIGH;
float temprop = 20;
float temp;
String inicial = "-" ;
String lectura = inicial;

void setup() {
  Serial.begin(9600);
  BT.begin(9600); //Velocidad del puerto del módulo Bluetooth
  sensorTemp.begin();   //Se inicia el sensor de temperatura
  pinMode(4, INPUT);
}

void loop() {
  estaon = digitalRead(4);
  if (estaon == LOW) {
    digitalWrite(13, LOW);
    if(BT.available()){
      lectura = BT.read();
      if(lectura == "84"){
        enviarTemp();
      }
      if(lectura == "80"){
        enviarPeso();
      }
      if(lectura == "83"){
        enviarPeso();
        enviarTemp();
      }
      if(lectura >= "100" && lectura <= "125" ){
        int num = ((100*(lectura.charAt(0)-48)) + (10*(lectura.charAt(1)-48)) + (lectura.charAt(2)-48));
        temprop = num % 100;
        Serial.print("Se establecio la Temperatura en: ");
        Serial.print(temprop);
        Serial.println(" °C");
      }
    }
    sensorTemp.requestTemperatures();   //Se envía el comando para leer la temperatura
    temp = sensorTemp.getTempCByIndex(0); //Se obtiene la temperatura en ºC
    
    if(temp > temprop){
      digitalWrite(10, HIGH); // enciendo el frio         reemplazar los 8 por 0 y los 9 por 1
      digitalWrite(9, LOW); // apago el calor 
    } 
    if (temp < temprop) {
      digitalWrite(9, HIGH); // enciendo el calor 
      digitalWrite(10, LOW); // apago el frio
    }
    if (temp > temprop+1) {
      digitalWrite(9, LOW); // apago el calor 
    }
    if (temp < temprop+1) {
      digitalWrite(10, LOW); // apago el frio
    }
  } else {
    digitalWrite(13, HIGH);
    digitalWrite(9, LOW);
    digitalWrite(10, LOW);
  }
  
}

void enviarTemp(){
  sensorTemp.requestTemperatures();   //Se envía el comando para leer la temperatura
  temp= sensorTemp.getTempCByIndex(0); //Se obtiene la temperatura en ºC
  Serial.print("Temperatura = ");
  Serial.print(temp);
  Serial.println(" °C");
  BT.write("Temperatura = ");
  BT.write(temp);
  BT.write(" °C");
}

void enviarPeso(){
  float peso = ((-1.6*(scale.getGram()))/1000);
  Serial.print("Peso Medido = ");
  Serial.print(peso);
  Serial.println(" Kg");
  BT.write("Peso Medido = ");
  BT.write(peso);
  BT.write(" Kg");
}
