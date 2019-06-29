#include "hx711.h"
#include <SoftwareSerial.h> //Librería que permite establecer comunicación serie en otros pins
#include <OneWire.h>                
#include <DallasTemperature.h>
#include <String.h>
#include <NewPing.h>

#define alto 25
#define ancho 20
#define profundidad 19 //en centimetros
#define TRIGGER_PIN  3
#define ECHO_PIN     5
#define TRIGGER_PIN2  6
#define ECHO_PIN2     9
#define TRIGGER_PIN3  10
#define ECHO_PIN3     11
#define MAX_DISTANCE 200

#define c 261
#define d 294
#define e 329
#define f 349
#define g 391
#define gS 415
#define a 440
#define aS 455
#define b 466
#define cH 523
#define cSH 554
#define dH 587
#define dSH 622
#define eH 659
#define fH 698
#define fSH 740
#define gH 784
#define gSH 830
#define aH 880


NewPing sonar(TRIGGER_PIN, ECHO_PIN, MAX_DISTANCE);
NewPing sonar2(TRIGGER_PIN2, ECHO_PIN2, MAX_DISTANCE);
NewPing sonar3(TRIGGER_PIN3, ECHO_PIN3, MAX_DISTANCE);
int luzint = 13;
int buzzer = 12;
int ventilacion = 16;
int frio = 17;
int calor = 18;
int magnetico = 4;
int tiempo1 = 0;
Hx711 scale(A1, A0); // Hx711.DOUT - pin #A1 Hx711.SCK - pin #A0
SoftwareSerial BT(7,8); // 7 RX, 8 TX.  Aquí conectamos los pins RXD,TDX del módulo Bluetooth.
OneWire ourWire(2); //Se establece el pin 2  como bus OneWire
DallasTemperature sensorTemp(&ourWire); //Se declara una variable u objeto para nuestro sensor
int estaon = HIGH;
float temprop = 20;
float temp;
String inicial = "-" ;
String lectura = inicial;
bool isSet = false;
unsigned long tiempo = 0;
unsigned long tiempoAlarma = 0;
String estado = "Apagado";
char msj[10];


void setup() {
  Serial.begin(9600);
  BT.begin(9600); //Velocidad del puerto del módulo Bluetooth
  sensorTemp.begin();   //Se inicia el sensor de temperatura
  pinMode(magnetico, INPUT);
  pinMode(luzint, OUTPUT);
  pinMode(buzzer, OUTPUT);
  pinMode(ventilacion, OUTPUT);
  pinMode(frio, OUTPUT);
  pinMode(calor, OUTPUT);
}

void loop() {
  estaon = digitalRead(magnetico);
  if (estaon == LOW) {
    digitalWrite(luzint, LOW);
    isSet = false;
    comprobarBt();
    sensorTemp.requestTemperatures();   //Se envía el comando para leer la temperatura
    temp = sensorTemp.getTempCByIndex(0); //Se obtiene la temperatura en ºC
    
    if(temp > (temprop+1)){
      encenderFrio();
    } 
    if (temp < temprop) {
      apagar(temp);
    }
    if (temp > temprop) {
      apagar(temp); 
    }
    if (temp < temprop-1) {
      encenderCalor();
    }
  } else {
    digitalWrite(luzint, HIGH);
    comprobarBt();
    apagar(LOW);
    if(!isSet)
    {
      tiempo = millis();
      isSet = true;
    }
    tiempoAlarma = millis();
    if(tiempoAlarma > (tiempo+15000)) 
    {
      march();
    }
  }
}

void comprobarBt(){
  if(BT.available()){
    lectura = BT.read();
    if(lectura == "84"){
      enviarTemp();
    }
    if(lectura == "80"){
      enviarPeso();
    }
    if(lectura == "86"){
      enviarVolumen();
    }
    if(lectura == "83"){
      enviarPeso();
      enviarTemp();
      enviarVolumen();
    }
    if(lectura >= "100" && lectura <= "125" ){
      int num = ((100*(lectura.charAt(0)-48)) + (10*(lectura.charAt(1)-48)) + (lectura.charAt(2)-48));
      temprop = num % 100;
    }
    if(lectura == "69"){
      enviarEstado();
    }
    if(lectura == "66"){
      apagarBuzzer();
    }
    if(lectura == "90"){
      enviarEstadoPuerta();
    }
  }
}

void enviarTemp(){
  sensorTemp.requestTemperatures();
  temp = sensorTemp.getTempCByIndex(0);
  sprintf(msj,"%s","T");
  dtostrf(temp,5,2,msj+1);
  sprintf(msj+6,"%s","|");
  BT.write(msj);
}

void apagarBuzzer(){
  isSet = false;
  digitalWrite(buzzer,LOW);
  return;
}

void enviarPeso(){
  float peso = ((-1.6*(scale.getGram()))/1000);
  if ( peso < 0 ){
    peso = 0.00;
  }
  sprintf(msj,"%s","P");
  dtostrf(peso,4,2,msj+1);
  sprintf(msj+5,"%s","|");
  BT.write(msj);
}

void enviarEstadoPuerta(){
  estaon = digitalRead(magnetico);
  if(estaon == LOW){
    BT.write("ZC|");
  } else {
    BT.write("ZA|");
  }
}

void enviarEstado(){
  if(estado == "Frio"){
    BT.write("E1|"); // Frio
  } else if (estado == "Calor"){
    BT.write("E2|"); // Calor
  } else {
    BT.write("E0|"); // Apagado
  }
}

void enviarVolumen(){
  float total;
  int uS = sonar.ping_median();
  total = (profundidad - (uS / US_ROUNDTRIP_CM));
  uS = sonar2.ping_median();
  total *= (ancho - (uS / US_ROUNDTRIP_CM));
  uS = sonar3.ping_median();
  total *= (alto - (uS / US_ROUNDTRIP_CM));
  sprintf(msj,"%s","V");
  dtostrf(total,5,2,msj+1);
  sprintf(msj+6,"%s","|");
  BT.write(msj);
}

void encenderFrio()
{
  if(estado != "Frio"){
    estado = "Frio";
    digitalWrite(frio, HIGH);
    digitalWrite(calor, LOW);
    digitalWrite(ventilacion, HIGH);
  }
}
void encenderCalor()
{
  if(estado != "Calor"){
    estado = "Calor";
    digitalWrite(frio, LOW);
    digitalWrite(calor, HIGH);
    digitalWrite(ventilacion, HIGH);
  }
}

void apagar(float tempMed)
{
  if (estado == "Frio" && (tempMed < temprop)){
    estado = "Apagado";
    digitalWrite(frio, LOW);
    digitalWrite(calor, LOW);
    digitalWrite(ventilacion, LOW);
  }
  if (estado == "Calor" && (tempMed > temprop)){
    estado = "Apagado";
    digitalWrite(frio, LOW);
    digitalWrite(calor, LOW);
    digitalWrite(ventilacion, LOW);
  } 
}

void apagar(int x)
{
  if (x == LOW && estado != "Apagado"){
    estado = "Apagado";
    digitalWrite(frio, LOW);
    digitalWrite(calor, LOW);
    digitalWrite(ventilacion, LOW);
  }
}

void beep (unsigned char buzzer, int frequencyInHertz, long timeInMilliseconds)
{ 
    digitalWrite(luzint, HIGH);  
    int x;
    long delayAmount = (long)(1000000/frequencyInHertz);
    long loopTime = (long)((timeInMilliseconds*1000)/(delayAmount*2));
    for (x=0;x<loopTime;x++)   
    {    
        estaon = digitalRead(magnetico);
        if (estaon == LOW){
          return;
        }
        digitalWrite(buzzer,HIGH);
        delayMicroseconds(delayAmount);
        digitalWrite(buzzer,LOW);
        delayMicroseconds(delayAmount);
    }    
    
    digitalWrite(luzint, LOW);
    delay(20);
}    
     
void march()
{    
    beep(buzzer, a, 500); 
    beep(buzzer, a, 500);     
    beep(buzzer, a, 500); 
    beep(buzzer, f, 350); 
    beep(buzzer, cH, 150);
    
    comprobarBt();
    estaon = digitalRead(magnetico);
    if (estaon == LOW || isSet == false){
      return;
    }
    
    beep(buzzer, a, 500);
    beep(buzzer, f, 350);
    beep(buzzer, cH, 150);
    beep(buzzer, a, 1000);

    comprobarBt();
    estaon = digitalRead(magnetico);
    if (estaon == LOW || isSet == false) {
      return;
    }
    
    beep(buzzer, eH, 500);
    beep(buzzer, eH, 500);
    beep(buzzer, eH, 500);    
    beep(buzzer, fH, 350); 
    beep(buzzer, cH, 150);

    comprobarBt();
    estaon = digitalRead(magnetico);
    if (estaon == LOW || isSet == false) {
      return;
    }
    
    beep(buzzer, gS, 500);
    beep(buzzer, f, 350);
    beep(buzzer, cH, 150);
    beep(buzzer, a, 1000);

    comprobarBt();
    estaon = digitalRead(magnetico);
    if (estaon == LOW || isSet == false) {
      return;
    }
    
    beep(buzzer, aH, 500);
    beep(buzzer, a, 350); 
    beep(buzzer, a, 150);
    beep(buzzer, aH, 500);
    beep(buzzer, gSH, 250); 
    beep(buzzer, gH, 250);

    comprobarBt();
    estaon = digitalRead(magnetico);
    if (estaon == LOW || isSet == false) {
      return;
    }
    
    beep(buzzer, fSH, 125);
    beep(buzzer, fH, 125);    
    beep(buzzer, fSH, 250);

    comprobarBt();
    estaon = digitalRead(magnetico);
    if (estaon == LOW || isSet == false) {
      return;
    }

    tiempo1=millis();
    while (millis()<(tiempo1+250)){
      comprobarBt();
      estaon = digitalRead(magnetico);
      if (estaon == LOW || isSet == false) {
        return;
      }
    }
    
    beep(buzzer, aS, 250);    
    beep(buzzer, dSH, 500);  
    beep(buzzer, dH, 250);  
    beep(buzzer, cSH, 250);
    
    comprobarBt();
    estaon = digitalRead(magnetico);
    if (estaon == LOW || isSet == false) {
      return;
    }
    
    beep(buzzer, cH, 125);  
    beep(buzzer, b, 125);  
    beep(buzzer, cH, 250);

    comprobarBt();
    estaon = digitalRead(magnetico);
    if (estaon == LOW || isSet == false) {
      return;
    }

    tiempo1=millis();
    while (millis()<(tiempo1+250)){
      comprobarBt();
      estaon = digitalRead(magnetico);
      if (estaon == LOW || isSet == false) {
        return;
      }
    }
    
    beep(buzzer, f, 125);  
    beep(buzzer, gS, 500);  
    beep(buzzer, f, 375);  
    beep(buzzer, a, 125);
    
    comprobarBt();
    estaon = digitalRead(magnetico);
    if (estaon == LOW || isSet == false) {
      return;
    }
    
    beep(buzzer, cH, 500); 
    beep(buzzer, a, 375);  
    beep(buzzer, cH, 125); 
    beep(buzzer, eH, 1000);

    comprobarBt();
    estaon = digitalRead(magnetico);
    if (estaon == LOW || isSet == false) {
      return;
    }
    
    beep(buzzer, aH, 500);
    beep(buzzer, a, 350); 
    beep(buzzer, a, 150);
    beep(buzzer, aH, 500);
    beep(buzzer, gSH, 250); 
    beep(buzzer, gH, 250);

    comprobarBt();
    estaon = digitalRead(magnetico);
    if (estaon == LOW || isSet == false) {
      return;
    }
    
    beep(buzzer, fSH, 125);
    beep(buzzer, fH, 125);    
    beep(buzzer, fSH, 250);

    comprobarBt();
    estaon = digitalRead(magnetico);
    if (estaon == LOW || isSet == false) {
      return;
    }
    
    tiempo1=millis();
    while (millis()<(tiempo1+250)){
      comprobarBt();
      estaon = digitalRead(magnetico);
      if (estaon == LOW || isSet == false) {
        return;
      }
    }
    
    beep(buzzer, aS, 250);    
    beep(buzzer, dSH, 500);  
    beep(buzzer, dH, 250);  
    beep(buzzer, cSH, 250);  

    comprobarBt();
    estaon = digitalRead(magnetico);
    if (estaon == LOW || isSet == false) {
      return;
    }
    
    beep(buzzer, cH, 125);  
    beep(buzzer, b, 125);  
    beep(buzzer, cH, 250);

    comprobarBt();
    estaon = digitalRead(magnetico);
    if (estaon == LOW || isSet == false) {
      return;
    }

    tiempo1=millis();
    while (millis()<(tiempo1+250)){
      comprobarBt();
      estaon = digitalRead(magnetico);
      if (estaon == LOW || isSet == false) {
        return;
      }
    }
    
    beep(buzzer, f, 250);  
    beep(buzzer, gS, 500);  
    beep(buzzer, f, 375);  
    beep(buzzer, cH, 125);

    comprobarBt();
    estaon = digitalRead(magnetico);
    if (estaon == LOW || isSet == false) {
      return;
    }
           
    beep(buzzer, a, 500);
    beep(buzzer, f, 375);
    beep(buzzer, c, 125);
    beep(buzzer, a, 1000);   
}
