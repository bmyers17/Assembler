int led1 = 0;
int led2 = 0;
int led3 = 0;
int oe_pm = 3;
int we_pm = 4;
int cp_pm = 5;
int oe_ab = 6;
int trans = 7;
int cp_pw = 8;
int op0 = 9;
int op1 = 10;
int op2 = 11;
int oe_sr = 2;
int pot = A0;
int ds = A1;
int sh_cp = A2;
int st_cp = A3;
int lower_pm = A4;
int higher_pm = A5;

int timeDel = 10;

void setup() 
{
  pinMode(led1, OUTPUT);
  pinMode(led2, OUTPUT);
  pinMode(led3, OUTPUT);
  pinMode(oe_pm, OUTPUT);
  pinMode(we_pm, OUTPUT);
  pinMode(cp_pm, OUTPUT);
  pinMode(oe_ab, OUTPUT);
  pinMode(trans, OUTPUT);
  pinMode(cp_pw, OUTPUT);
  pinMode(op0, INPUT);
  pinMode(op1, INPUT);
  pinMode(op2, INPUT);
  pinMode(oe_sr, OUTPUT);
  pinMode(pot, INPUT);
  pinMode(ds, OUTPUT);
  pinMode(sh_cp, OUTPUT);
  pinMode(st_cp, OUTPUT);
  pinMode(lower_pm, OUTPUT);
  pinMode(higher_pm, OUTPUT);
  
  digitalWrite(oe_pm, HIGH);
  digitalWrite(we_pm, HIGH);
  digitalWrite(cp_pm, LOW);
  digitalWrite(oe_ab, HIGH);
  digitalWrite(trans, LOW);
  digitalWrite(cp_pw, LOW);
  digitalWrite(ds, LOW);
  digitalWrite(sh_cp, LOW);
  digitalWrite(st_cp, LOW);
  digitalWrite(oe_sr, LOW);
  digitalWrite(lower_pm, LOW);
  digitalWrite(higher_pm, LOW);
  
  delay(timeDelay());
} 

void load(int address, int instruction)
{
  digitalWrite(cp_pm, LOW);
  loadAddress(address);
  digitalWrite(cp_pm, HIGH);
  
  digitalWrite(we_pm, HIGH);
  shiftValue(instruction);
  digitalWrite(we_pm, LOW);
}

void loadAddress(int address)
{
  int higher = address / 256;
  int lower = address % 256;
  
  digitalWrite(lower_pm, LOW);
  shiftValue(lower);
  digitalWrite(lower_pm, HIGH);
  
  digitalWrite(higher_pm, LOW);
  shiftValue(higher);
  digitalWrite(higher_pm, HIGH);
}

void shiftValue(int value)
{
  digitalWrite(st_cp, LOW);
  shiftOut(ds, sh_cp, MSBFIRST, value);
  digitalWrite(st_cp, HIGH);
}

int timeDelay()
{
  return timeDel;
}

void loop() {} 

