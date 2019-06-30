// Bibliotecas
// --------------------------------------------------------------------------------

#include "hx711.h"             // para sensar el peso
#include <SoftwareSerial.h>    // para la comunicación bluetooth
#include <OneWire.h>           // para sensar la temperatura
#include <DallasTemperature.h> // para sensar la temperatura
#include <NewPing.h>           // para obtener el volumen (con los sensores de ultrasonido)
#include <String.h>

// Constantes
// --------------------------------------------------------------------------------

// dimensiones totales del compartimento
#define DIMENSION_CENTIMETROS_ALTO 25
#define DIMENSION_CENTIMETROS_ANCHO 20
#define DIMENSION_CENTIMETROS_PROFUNDIDAD 19
// disntancia máxima en centímetros que se puede obtener
#define DISTANCIA_MAXIMA_CENTIMETROS 200

// pines
#define PIN_LUZ_INTERNA 13
#define PIN_BUZZER 12
#define PIN_VENTILADORES 16
#define PIN_PELTIER_ENFRIAR 17
#define PIN_PELTIER_CALENTAR 18
#define PIN_SWITCH_PUERTA 4
#define PIN_BUS_SENSOR_TEMPERATURA 2
#define PIN_BLUETOOTH_RX 7
#define PIN_BLUETOOTH_TX 8
// pines de sensores de ultrasonido
#define PIN_SONAR_1_TRIGGER 3
#define PIN_SONAR_1_ECHO 5
#define PIN_SONAR_2_TRIGGER 6
#define PIN_SONAR_2_ECHO 9
#define PIN_SONAR_3_TRIGGER 10
#define PIN_SONAR_3_ECHO 11

// frecuencias de sonido para la alarma
#define ALARMA_FRECUENCIA_c 261
#define ALARMA_FRECUENCIA_f 349
#define ALARMA_FRECUENCIA_gS 415
#define ALARMA_FRECUENCIA_a 440
#define ALARMA_FRECUENCIA_aS 455
#define ALARMA_FRECUENCIA_b 466
#define ALARMA_FRECUENCIA_cH 523
#define ALARMA_FRECUENCIA_cSH 554
#define ALARMA_FRECUENCIA_dH 587
#define ALARMA_FRECUENCIA_dSH 622
#define ALARMA_FRECUENCIA_eH 659
#define ALARMA_FRECUENCIA_fH 698
#define ALARMA_FRECUENCIA_fSH 740
#define ALARMA_FRECUENCIA_gH 784
#define ALARMA_FRECUENCIA_gSH 830
#define ALARMA_FRECUENCIA_aH 880

// segundos que la puerta puede estar abierta antes de que comience a sonar la alarma
#define ALARMA_TOLERANCIA_SEGUNDOS 15

// estados posibles
#define ESTADO_PUERTA_ABIERTA HIGH
#define ESTADO_PUERTA_CERRADA LOW
#define ESTADO_PELTIER_APAGADO "PELTIER_APAGADO"       // temperatura de mantenimiento alcanzada
#define ESTADO_PELTIER_ENFRIANDO "PELTIER_ENFRIANDO"   // se debe enfriar el compartimento
#define ESTADO_PELTIER_CALENTANDO "PELTIER_CALENTANDO" // se debe calentar el compartimento

// temperatura a la que se mantiene el compartimento por defecto
#define TEMPERATURA_INICIAL_CELSIUS 20
// grados de tolerancia de la temperatura a la que se debe mantener el compartimento
#define TEMPERATURA_TOLERANCIA_CELSIUS 2

// comandos entrantes del protocolo de comunicación
#define COMANDO_INDEFINIDO "-"             // ningún comando fue asignado
#define COMANDO_ENVIAR_TEMPERATURA "84"    // responde la temperatura actual del compartimento
#define COMANDO_ENVIAR_PESO "80"           // responde el peso sensado
#define COMANDO_ENVIAR_VOLUMEN "86"        // responde el volumen calculado
#define COMANDO_ENVIAR_DATOS "83"          // enviar peso|temperatura|volumen
#define COMANDO_TEMPERATURA_MINIMA "100"   // temperatura de mantenimiento mínima
#define COMANDO_TEMPERATURA_MAXIMA "125"   // temperatura de mantenimiento maxima
#define COMANDO_ENVIAR_ESTADO_PELTIER "69" // enviar estado de la celda peltier
#define COMANDO_APAGAR_ALARMA "66"
#define COMANDO_ENVIAR_ESTADO_PUERTA "90" // enviar estado de la puerta "ABIERTA|CERRADA"

// comandos salientes del protocolo de comunicación
#define COMANDO_ESTADO_PELTIER_ENFRIANDO "E1|"  // cuando se esta enfriando el compartimento
#define COMANDO_ESTADO_PELTIER_CALENTANDO "E2|" // cuando se esta calentando el compartimento
#define COMANDO_ESTADO_PELTIER_APAGADO "E0|"    // cuando el compartimento no se debe ni enfriar ni calentar
#define COMANDO_ESTADO_PUERTA_ABIERTA "ZA|"     // cuando la puerta está abierta
#define COMANDO_ESTADO_PUERTA_CERRADA "ZC|"     // cuando la puerta está cerrada

// Variables globales
// --------------------------------------------------------------------------------

// contiene el último comando entrante desde desde el bluetooth
String comando = COMANDO_INDEFINIDO;

// cadena de texto con el mensaje que se envía por bluetooth
char mensaje_bluetooth[10];

// declarar sensores de ultrasonido
NewPing sonar_1(PIN_SONAR_1_TRIGGER, PIN_SONAR_1_ECHO, DISTANCIA_MAXIMA_CENTIMETROS);
NewPing sonar_2(PIN_SONAR_2_TRIGGER, PIN_SONAR_2_ECHO, DISTANCIA_MAXIMA_CENTIMETROS);
NewPing sonar_3(PIN_SONAR_3_TRIGGER, PIN_SONAR_3_ECHO, DISTANCIA_MAXIMA_CENTIMETROS);

// declarar sensor de temperatura
OneWire busSensorTemperatura(PIN_BUS_SENSOR_TEMPERATURA);
DallasTemperature sensorTemperatura(&busSensorTemperatura);

// declarar objeto de comunicación bluetooth
SoftwareSerial bluetooth(PIN_BLUETOOTH_RX, PIN_BLUETOOTH_TX);

// declarar sensor de peso (A1 y A0 son pines analógicos declarados en la librería hx711)
Hx711 balanza(A1, A0);

// contiene el estado de la peurta ya sea ABIERTA o CERRADA
int puerta_estado = ESTADO_PUERTA_ABIERTA;

// temperatura a la que debe mantenerse el compartimento
float temperatura_de_mantenimiento = TEMPERATURA_INICIAL_CELSIUS;
float temperatura_sensada_celsius;

// contiene el estado de la celda peltier ya sea APAGADO, ENFRIANDO o CALENTANDO
String peltier_estado = ESTADO_PELTIER_APAGADO;

// tiempo que debe sonar una frecuencia de la alarma, en milisegundos
unsigned long alarma_frecuencia_millis;
// vale "true" cuando la alarma puede estar encendida
// vale "false" cuando la alarma no puede estar encendida
bool alarma_puede_estar_encendida = false;
// tiempo de tolerancia transcurrido para saber si debo encender un aviso de puerta abierta
unsigned long alarma_tolerancia_millis;

/**
 * asignación de valores de la configuración inicial
 */
void setup()
{
  // velocidad en baudios de la comunicación serie con los periféricos
  Serial.begin(9600);
  bluetooth.begin(9600);
  sensorTemperatura.begin();

  // inicialización de pines como entrada o salida
  pinMode(PIN_SWITCH_PUERTA, INPUT);
  pinMode(PIN_LUZ_INTERNA, OUTPUT);
  pinMode(PIN_BUZZER, OUTPUT);
  pinMode(PIN_VENTILADORES, OUTPUT);
  pinMode(PIN_PELTIER_ENFRIAR, OUTPUT);
  pinMode(PIN_PELTIER_CALENTAR, OUTPUT);
}

void loop()
{
  puerta_estado = digitalRead(PIN_SWITCH_PUERTA);
  if (puerta_estado == ESTADO_PUERTA_CERRADA)
  {
    digitalWrite(PIN_LUZ_INTERNA, LOW);
    alarma_puede_estar_encendida = false;
    blueoothRevisarComandoEntrante();
    sensorTemperatura.requestTemperatures();
    temperatura_sensada_celsius = sensorTemperatura.getTempCByIndex(0);
    if (temperatura_sensada_celsius > (temperatura_de_mantenimiento + TEMPERATURA_TOLERANCIA_CELSIUS))
    {
      peltierEncenderEnfriar();
    }
    if (temperatura_sensada_celsius < temperatura_de_mantenimiento)
    {
      peltierApagar(temperatura_sensada_celsius);
    }
    if (temperatura_sensada_celsius > temperatura_de_mantenimiento)
    {
      peltierApagar(temperatura_sensada_celsius);
    }
    if (temperatura_sensada_celsius < temperatura_de_mantenimiento - TEMPERATURA_TOLERANCIA_CELSIUS)
    {
      peltierEncenderCalentar();
    }
  }
  else
  {
    digitalWrite(PIN_LUZ_INTERNA, HIGH);
    blueoothRevisarComandoEntrante();
    peltierApagar(LOW);
    if (!alarma_puede_estar_encendida)
    {
      alarma_tolerancia_millis = millis();
      alarma_puede_estar_encendida = true;
    }
    if (millis() > (alarma_tolerancia_millis + ALARMA_TOLERANCIA_SEGUNDOS * 1000))
    {
      march();
    }
  }
}

void blueoothRevisarComandoEntrante()
{
  if (bluetooth.available())
  {
    comando = bluetooth.read();
    if (comando == COMANDO_ENVIAR_TEMPERATURA)
    {
      enviarTemperatura();
    }
    if (comando == COMANDO_ENVIAR_PESO)
    {
      enviarPeso();
    }
    if (comando == COMANDO_ENVIAR_VOLUMEN)
    {
      enviarVolumen();
    }
    if (comando == COMANDO_ENVIAR_DATOS)
    {
      enviarPeso();
      enviarTemperatura();
      enviarVolumen();
    }
    if (comando >= COMANDO_TEMPERATURA_MINIMA && comando <= COMANDO_TEMPERATURA_MAXIMA)
    {
      int temperatura_recibida = ((100 * (comando.charAt(0) - 48)) + (10 * (comando.charAt(1) - 48)) + (comando.charAt(2) - 48));
      // asignar nueva temperatura a la que se debe mantener el compartimento
      temperatura_de_mantenimiento = temperatura_recibida % 100;
    }
    if (comando == COMANDO_ENVIAR_ESTADO_PELTIER)
    {
      enviarPeltierEstado();
    }
    if (comando == COMANDO_APAGAR_ALARMA)
    {
      apagarAlarma();
    }
    if (comando == COMANDO_ENVIAR_ESTADO_PUERTA)
    {
      enviarEstadoPuerta();
    }
  }
}

void enviarTemperatura()
{
  sensorTemperatura.requestTemperatures();
  temperatura_sensada_celsius = sensorTemperatura.getTempCByIndex(0);
  sprintf(mensaje_bluetooth, "%s", "T");
  dtostrf(temperatura_sensada_celsius, 5, 2, mensaje_bluetooth + 1);
  sprintf(mensaje_bluetooth + 6, "%s", "|");
  bluetooth.write(mensaje_bluetooth);
}

void apagarAlarma()
{
  alarma_puede_estar_encendida = false;
  digitalWrite(PIN_BUZZER, LOW);
  return;
}

void enviarPeso()
{
  float peso = ((-1.6 * (balanza.getGram())) / 1000);
  if (peso < 0)
  {
    peso = 0.00;
  }
  sprintf(mensaje_bluetooth, "%s", "P");
  dtostrf(peso, 4, 2, mensaje_bluetooth + 1);
  sprintf(mensaje_bluetooth + 5, "%s", "|");
  bluetooth.write(mensaje_bluetooth);
}

void enviarEstadoPuerta()
{
  puerta_estado = digitalRead(PIN_SWITCH_PUERTA);
  if (puerta_estado == ESTADO_PUERTA_CERRADA)
  {
    bluetooth.write(COMANDO_ESTADO_PUERTA_CERRADA);
  }
  else
  {
    bluetooth.write(COMANDO_ESTADO_PUERTA_ABIERTA);
  }
}

void enviarPeltierEstado()
{
  if (peltier_estado == ESTADO_PELTIER_ENFRIANDO)
  {
    bluetooth.write(COMANDO_ESTADO_PELTIER_ENFRIANDO);
  }
  else if (peltier_estado == ESTADO_PELTIER_CALENTANDO)
  {
    bluetooth.write(COMANDO_ESTADO_PELTIER_CALENTANDO);
  }
  else
  {
    bluetooth.write(COMANDO_ESTADO_PELTIER_APAGADO);
  }
}

/**
 * obtener la distancia ocupada en cada eje (X,Y,Z)
 * multiplicandolas entre si para obtener el volumen ocupado
 */
void enviarVolumen()
{
  float total;
  int uS = sonar_1.ping_median();
  total = (DIMENSION_CENTIMETROS_PROFUNDIDAD - (uS / US_ROUNDTRIP_CM));
  uS = sonar_2.ping_median();
  total *= (DIMENSION_CENTIMETROS_ANCHO - (uS / US_ROUNDTRIP_CM));
  uS = sonar_3.ping_median();
  total *= (DIMENSION_CENTIMETROS_ALTO - (uS / US_ROUNDTRIP_CM));
  sprintf(mensaje_bluetooth, "%s", "V");
  dtostrf(total, 5, 2, mensaje_bluetooth + 1);
  sprintf(mensaje_bluetooth + 6, "%s", "|");
  bluetooth.write(mensaje_bluetooth);
}

void peltierEncenderEnfriar()
{
  if (peltier_estado != ESTADO_PELTIER_ENFRIANDO)
  {
    peltier_estado = ESTADO_PELTIER_ENFRIANDO;
    digitalWrite(PIN_PELTIER_ENFRIAR, HIGH);
    digitalWrite(PIN_PELTIER_CALENTAR, LOW);
    digitalWrite(PIN_VENTILADORES, HIGH);
  }
}
void peltierEncenderCalentar()
{
  if (peltier_estado != ESTADO_PELTIER_CALENTANDO)
  {
    peltier_estado = ESTADO_PELTIER_CALENTANDO;
    digitalWrite(PIN_PELTIER_ENFRIAR, LOW);
    digitalWrite(PIN_PELTIER_CALENTAR, HIGH);
    digitalWrite(PIN_VENTILADORES, HIGH);
  }
}

void peltierApagar(float tempMed)
{
  if (peltier_estado == ESTADO_PELTIER_ENFRIANDO && (tempMed < temperatura_de_mantenimiento))
  {
    peltier_estado = ESTADO_PELTIER_APAGADO;
    digitalWrite(PIN_PELTIER_ENFRIAR, LOW);
    digitalWrite(PIN_PELTIER_CALENTAR, LOW);
    digitalWrite(PIN_VENTILADORES, LOW);
  }
  if (peltier_estado == ESTADO_PELTIER_CALENTANDO && (tempMed > temperatura_de_mantenimiento))
  {
    peltier_estado = ESTADO_PELTIER_APAGADO;
    digitalWrite(PIN_PELTIER_ENFRIAR, LOW);
    digitalWrite(PIN_PELTIER_CALENTAR, LOW);
    digitalWrite(PIN_VENTILADORES, LOW);
  }
}

void peltierApagar(int x)
{
  if (x == LOW && peltier_estado != ESTADO_PELTIER_APAGADO)
  {
    peltier_estado = ESTADO_PELTIER_APAGADO;
    digitalWrite(PIN_PELTIER_ENFRIAR, LOW);
    digitalWrite(PIN_PELTIER_CALENTAR, LOW);
    digitalWrite(PIN_VENTILADORES, LOW);
  }
}

/**
 * emite una frecuencia a través del buzzer, durante un tiempo en milisegundos
 */
void beep(int frecuencia_hertz, long tiempo)
{
  digitalWrite(PIN_LUZ_INTERNA, HIGH);
  int x;
  long delay_microsegundos = (long)(1000000 / frecuencia_hertz);
  long loopTime = (long)((tiempo * 1000) / (delay_microsegundos * 2));
  for (x = 0; x < loopTime; x++)
  {
    puerta_estado = digitalRead(PIN_SWITCH_PUERTA);
    if (puerta_estado == ESTADO_PUERTA_CERRADA)
    {
      return;
    }
    digitalWrite(PIN_BUZZER, HIGH);
    delayMicroseconds(delay_microsegundos);
    digitalWrite(PIN_BUZZER, LOW);
    delayMicroseconds(delay_microsegundos);
  }
  digitalWrite(PIN_LUZ_INTERNA, LOW);
  delay(20);
}

void march()
{
  beep(ALARMA_FRECUENCIA_a, 500);
  beep(ALARMA_FRECUENCIA_a, 500);
  beep(ALARMA_FRECUENCIA_a, 500);
  beep(ALARMA_FRECUENCIA_f, 350);
  beep(ALARMA_FRECUENCIA_cH, 150);

  blueoothRevisarComandoEntrante();
  puerta_estado = digitalRead(PIN_SWITCH_PUERTA);
  if (puerta_estado == ESTADO_PUERTA_CERRADA || alarma_puede_estar_encendida == false)
  {
    return;
  }

  beep(ALARMA_FRECUENCIA_a, 500);
  beep(ALARMA_FRECUENCIA_f, 350);
  beep(ALARMA_FRECUENCIA_cH, 150);
  beep(ALARMA_FRECUENCIA_a, 1000);

  blueoothRevisarComandoEntrante();
  puerta_estado = digitalRead(PIN_SWITCH_PUERTA);
  if (puerta_estado == ESTADO_PUERTA_CERRADA || alarma_puede_estar_encendida == false)
  {
    return;
  }

  beep(ALARMA_FRECUENCIA_eH, 500);
  beep(ALARMA_FRECUENCIA_eH, 500);
  beep(ALARMA_FRECUENCIA_eH, 500);
  beep(ALARMA_FRECUENCIA_fH, 350);
  beep(ALARMA_FRECUENCIA_cH, 150);

  blueoothRevisarComandoEntrante();
  puerta_estado = digitalRead(PIN_SWITCH_PUERTA);
  if (puerta_estado == ESTADO_PUERTA_CERRADA || alarma_puede_estar_encendida == false)
  {
    return;
  }

  beep(ALARMA_FRECUENCIA_gS, 500);
  beep(ALARMA_FRECUENCIA_f, 350);
  beep(ALARMA_FRECUENCIA_cH, 150);
  beep(ALARMA_FRECUENCIA_a, 1000);

  blueoothRevisarComandoEntrante();
  puerta_estado = digitalRead(PIN_SWITCH_PUERTA);
  if (puerta_estado == ESTADO_PUERTA_CERRADA || alarma_puede_estar_encendida == false)
  {
    return;
  }

  beep(ALARMA_FRECUENCIA_aH, 500);
  beep(ALARMA_FRECUENCIA_a, 350);
  beep(ALARMA_FRECUENCIA_a, 150);
  beep(ALARMA_FRECUENCIA_aH, 500);
  beep(ALARMA_FRECUENCIA_gSH, 250);
  beep(ALARMA_FRECUENCIA_gH, 250);

  blueoothRevisarComandoEntrante();
  puerta_estado = digitalRead(PIN_SWITCH_PUERTA);
  if (puerta_estado == ESTADO_PUERTA_CERRADA || alarma_puede_estar_encendida == false)
  {
    return;
  }

  beep(ALARMA_FRECUENCIA_fSH, 125);
  beep(ALARMA_FRECUENCIA_fH, 125);
  beep(ALARMA_FRECUENCIA_fSH, 250);

  blueoothRevisarComandoEntrante();
  puerta_estado = digitalRead(PIN_SWITCH_PUERTA);
  if (puerta_estado == ESTADO_PUERTA_CERRADA || alarma_puede_estar_encendida == false)
  {
    return;
  }

  alarma_frecuencia_millis = millis();
  while (millis() < (alarma_frecuencia_millis + 250))
  {
    blueoothRevisarComandoEntrante();
    puerta_estado = digitalRead(PIN_SWITCH_PUERTA);
    if (puerta_estado == ESTADO_PUERTA_CERRADA || alarma_puede_estar_encendida == false)
    {
      return;
    }
  }

  beep(ALARMA_FRECUENCIA_aS, 250);
  beep(ALARMA_FRECUENCIA_dSH, 500);
  beep(ALARMA_FRECUENCIA_dH, 250);
  beep(ALARMA_FRECUENCIA_cSH, 250);

  blueoothRevisarComandoEntrante();
  puerta_estado = digitalRead(PIN_SWITCH_PUERTA);
  if (puerta_estado == ESTADO_PUERTA_CERRADA || alarma_puede_estar_encendida == false)
  {
    return;
  }

  beep(ALARMA_FRECUENCIA_cH, 125);
  beep(ALARMA_FRECUENCIA_b, 125);
  beep(ALARMA_FRECUENCIA_cH, 250);

  blueoothRevisarComandoEntrante();
  puerta_estado = digitalRead(PIN_SWITCH_PUERTA);
  if (puerta_estado == ESTADO_PUERTA_CERRADA || alarma_puede_estar_encendida == false)
  {
    return;
  }

  alarma_frecuencia_millis = millis();
  while (millis() < (alarma_frecuencia_millis + 250))
  {
    blueoothRevisarComandoEntrante();
    puerta_estado = digitalRead(PIN_SWITCH_PUERTA);
    if (puerta_estado == ESTADO_PUERTA_CERRADA || alarma_puede_estar_encendida == false)
    {
      return;
    }
  }

  beep(ALARMA_FRECUENCIA_f, 125);
  beep(ALARMA_FRECUENCIA_gS, 500);
  beep(ALARMA_FRECUENCIA_f, 375);
  beep(ALARMA_FRECUENCIA_a, 125);

  blueoothRevisarComandoEntrante();
  puerta_estado = digitalRead(PIN_SWITCH_PUERTA);
  if (puerta_estado == ESTADO_PUERTA_CERRADA || alarma_puede_estar_encendida == false)
  {
    return;
  }

  beep(ALARMA_FRECUENCIA_cH, 500);
  beep(ALARMA_FRECUENCIA_a, 375);
  beep(ALARMA_FRECUENCIA_cH, 125);
  beep(ALARMA_FRECUENCIA_eH, 1000);

  blueoothRevisarComandoEntrante();
  puerta_estado = digitalRead(PIN_SWITCH_PUERTA);
  if (puerta_estado == ESTADO_PUERTA_CERRADA || alarma_puede_estar_encendida == false)
  {
    return;
  }

  beep(ALARMA_FRECUENCIA_aH, 500);
  beep(ALARMA_FRECUENCIA_a, 350);
  beep(ALARMA_FRECUENCIA_a, 150);
  beep(ALARMA_FRECUENCIA_aH, 500);
  beep(ALARMA_FRECUENCIA_gSH, 250);
  beep(ALARMA_FRECUENCIA_gH, 250);

  blueoothRevisarComandoEntrante();
  puerta_estado = digitalRead(PIN_SWITCH_PUERTA);
  if (puerta_estado == ESTADO_PUERTA_CERRADA || alarma_puede_estar_encendida == false)
  {
    return;
  }

  beep(ALARMA_FRECUENCIA_fSH, 125);
  beep(ALARMA_FRECUENCIA_fH, 125);
  beep(ALARMA_FRECUENCIA_fSH, 250);

  blueoothRevisarComandoEntrante();
  puerta_estado = digitalRead(PIN_SWITCH_PUERTA);
  if (puerta_estado == ESTADO_PUERTA_CERRADA || alarma_puede_estar_encendida == false)
  {
    return;
  }

  alarma_frecuencia_millis = millis();
  while (millis() < (alarma_frecuencia_millis + 250))
  {
    blueoothRevisarComandoEntrante();
    puerta_estado = digitalRead(PIN_SWITCH_PUERTA);
    if (puerta_estado == ESTADO_PUERTA_CERRADA || alarma_puede_estar_encendida == false)
    {
      return;
    }
  }

  beep(ALARMA_FRECUENCIA_aS, 250);
  beep(ALARMA_FRECUENCIA_dSH, 500);
  beep(ALARMA_FRECUENCIA_dH, 250);
  beep(ALARMA_FRECUENCIA_cSH, 250);

  blueoothRevisarComandoEntrante();
  puerta_estado = digitalRead(PIN_SWITCH_PUERTA);
  if (puerta_estado == ESTADO_PUERTA_CERRADA || alarma_puede_estar_encendida == false)
  {
    return;
  }

  beep(ALARMA_FRECUENCIA_cH, 125);
  beep(ALARMA_FRECUENCIA_b, 125);
  beep(ALARMA_FRECUENCIA_cH, 250);

  blueoothRevisarComandoEntrante();
  puerta_estado = digitalRead(PIN_SWITCH_PUERTA);
  if (puerta_estado == ESTADO_PUERTA_CERRADA || alarma_puede_estar_encendida == false)
  {
    return;
  }

  alarma_frecuencia_millis = millis();
  while (millis() < (alarma_frecuencia_millis + 250))
  {
    blueoothRevisarComandoEntrante();
    puerta_estado = digitalRead(PIN_SWITCH_PUERTA);
    if (puerta_estado == ESTADO_PUERTA_CERRADA || alarma_puede_estar_encendida == false)
    {
      return;
    }
  }

  beep(ALARMA_FRECUENCIA_f, 250);
  beep(ALARMA_FRECUENCIA_gS, 500);
  beep(ALARMA_FRECUENCIA_f, 375);
  beep(ALARMA_FRECUENCIA_cH, 125);

  blueoothRevisarComandoEntrante();
  puerta_estado = digitalRead(PIN_SWITCH_PUERTA);
  if (puerta_estado == ESTADO_PUERTA_CERRADA || alarma_puede_estar_encendida == false)
  {
    return;
  }

  beep(ALARMA_FRECUENCIA_a, 500);
  beep(ALARMA_FRECUENCIA_f, 375);
  beep(ALARMA_FRECUENCIA_c, 125);
  beep(ALARMA_FRECUENCIA_a, 1000);
}
