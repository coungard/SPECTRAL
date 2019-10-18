# coding=utf-8

import time, random
from os import system
from PyAutoItPy import AutoItX, WinHandle, WinState, MB_Flags

account_string = ""
sum_string = ""
note_string = ""
status_string = ""
status = ""
x = 0
y = 1
fileName='payment'
with open(fileName, 'r') as file:

    data = file.readlines()

print (data)
print ("Your name: " + data[0])


for str in data:
    if "number" == str[0:6]:
        account_string = str
        acc_list = list(account_string)
    if "sum" == str[0:3]:
        sum_string = str
    if "text" == str[0:4]:
        note_string = str
        note_list = list(note_string)
    if "status" == str[0:6]:
        status_string = str
        status = status_string.lower().split("=")[1].strip()


print("1. ", account_string)
print("2. ", sum_string)
print("3. ", note_string)
print("4. ", status_string)

print("Set status in ACCEPTED -> LOADING ", )
if status == "accepted":
    data[1]="status=LOADING\n"
    status="loading"
    with open(fileName, 'w') as file:
        file.writelines(data)
print("status = ", status )
input()
'''
def randomValue(currV):
    cV= int(currV)
    
    print("text::",random.randint(cV-10,currV+10))
    return str(random.randint(cV-10,currV+10))
'''
strButton = {" ": "550 711",
             "й": "140 432",#randomValue(140)+ "+"+randomValue(432),
             "ц": "643 432",
             "у": "773 432",
             "к": "514 432",
             "е": "638 432",
             "н": "773 432",
             "г": "514 432",
             "ш": "646 432",
             "щ": "760 432",
             "з": "1065 432",
             "х": "1065 432",
             "ъ": "617 432",

             "ф": "512 530",
             "ы": "643 530",
             "в": "773 530",
             "а": "514 530",
             "п": "638 530",
             "р": "773 530",
             "о": "514 530",
             "л": "646 530",
             "д": "760 530",
             "ж": "1065 530",
             "э": "1065 530",

             "я": "617 621",
             "ч": "512 621",
             "с": "643 621",
             "м": "773 621",
             "и": "514 621",
             "т": "638 621",
             "ь": "773 621",
             "б": "514 621",
             "ю": "646 621",
             ".": "760 621",
             "ё": "1065 621",
             "х": "1065 621",
             "ъ": "617 621",
             "CancelOrange": "637 579",
             "Confirm": "787 748"}

iButton = {"0": "640 731",
           "1": "512 373",
           "2": "643 381",
           "3": "773 373",
           "4": "514 491",
           "5": "638 495",
           "6": "773 498",
           "7": "514 617",
           "8": "646 617",
           "9": "760 614",
           "Next": "1065 965",
           "Back": "1065 965",
           "Main": "617 961",
           "CancelOrange": "637 579",
           "Confirm": "787 748"}

print(iButton["9"].split(" ")[x])
print(iButton["9"].split(" ")[y])

Title = '[CLASS:ATL:009CC440]'

Automat = AutoItX()
Control = '[CLASS:MozillaWindowClass; INSTANCE:2]'
Handle = None

# Automat.AutoItSetOption("MouseCoordMode",0)

qWindowIsOpened = Automat.WinExists("OSMP Browser", "")
if qWindowIsOpened:
    print("success")

Opened = Automat.WinWait(Title, 5)
if not Opened:
    print('qiwi not open')
    exit(-1)

Handle = WinHandle(Automat.WinGetHandle(Title))
if not Handle:
    print('Невозможно получить Handle qiwi')
    exit(-1)
print('Handle окна qiwi: {}'.format(Handle))
State = WinState(Automat.WinGetState(Handle))
print('Состояние окна OSMP Browser: {} {}'.format(State.StateNum, State.StateString))

Automat.ControlClick(Title, Control, 432, 675, 1, "left", "")  #
print("qiwi button")
time.sleep(5)
Automat.ControlClick(Title, Control, 406, 514, 1, "left", "")  #
print("add qiwi money")
time.sleep(5)
Automat.ControlClick(Title, Control, int(iButton["9"].split(" ")[x]), int(iButton["9"].split(" ")[y]), 1, "left", "")  # 9
print("9")
time.sleep(2)
Automat.ControlClick(Title, Control, int(iButton["2"].split(" ")[x]), int(iButton["2"].split(" ")[y]), 1, "left", "")  # 2
print("2")
time.sleep(3)
Automat.ControlClick(Title, Control, int(iButton["8"].split(" ")[x]), int(iButton["8"].split(" ")[y]), 1, "left", "")  # 8
print("8")
time.sleep(2)
Automat.ControlClick(Title, Control, int(iButton["5"].split(" ")[x]), int(iButton["5"].split(" ")[y]), 1, "left", "")  # 5
print("5")
time.sleep(2)
Automat.ControlClick(Title, Control, int(iButton["6"].split(" ")[x]), int(iButton["6"].split(" ")[y]), 1, "left", "")  # 6
print("6")
time.sleep(2)

Automat.ControlClick(Title, Control, int(iButton["8"].split(" ")[x]), int(iButton["8"].split(" ")[y]), 1, "left", "")  # 8
print("8")
time.sleep(2)

Automat.ControlClick(Title, Control, int(iButton["5"].split(" ")[x]), int(iButton["5"].split(" ")[y]), 1, "left", "")  # 5
print("5")
time.sleep(2)
Automat.ControlClick(Title, Control, int(iButton["4"].split(" ")[x]), int(iButton["4"].split(" ")[y]), 1, "left", "")  # 4
print("4")
time.sleep(2)
Automat.ControlClick(Title, Control, int(iButton["4"].split(" ")[x]), int(iButton["4"].split(" ")[y]), 1, "left", "")  # 4
print("4")
time.sleep(2)
Automat.ControlClick(Title, Control, int(iButton["5"].split(" ")[x]), int(iButton["5"].split(" ")[y]), 1, "left", "")  # 5
print("5")
time.sleep(2)

'''
page where printing avatar
631 606
color=0xEFEBE7
grey
'''
Automat.ControlClick(Title, Control, int(iButton["Next"].split(" ")[x]), int(iButton["Next"].split(" ")[y]), 1, "left",
                     "")  # Next
print("Next")
time.sleep(5)
color = Automat.PixelGetColor(516, 835)
if color == 30141:
    print("Color_code: 30141")
while color == 30141:
    color = Automat.PixelGetColor(516, 835)
time.sleep(3)
'''
modal window confirm
690 729
color=0xFFFFFF
white
'''
Automat.ControlClick(Title, Control, int(iButton["Confirm"].split(" ")[x]), int(iButton["Confirm"].split(" ")[y]), 1, "left", "")  # Verno
print("Verno")
time.sleep(3)

Automat.ControlClick(Title, Control, int(iButton["Next"].split(" ")[x]), int(iButton["Next"].split(" ")[y]), 1, "left", "")  # Next
print("Next")
time.sleep(5)

print("Set status in LOADING -> COMPLETED ", )
if status == "loading":
    data[1]="status=COMPLETED\n"
    with open(fileName, 'w') as file:
        file.writelines(data)
        status='comleted'
print("status was set, status= ",status)




print("waiting status  STACKING... ")
while status != "stacking":
    time.sleep(1)
    with open(fileName, 'r') as file:
        data = file.readlines()
        for str in data:
            if "status" == str[0:6]:
                status_string = str
                status = status_string.lower().split("=")[1].strip()
print("status set STACKING, continue work with status= ", status)

print("press button payment...")
Automat.ControlClick(Title, Control, int(iButton["Next"].split(" ")[x]), int(iButton["Next"].split(" ")[y]), 1, "left", "")  # MainPage
print("Main page")


print("Set status in STACKING -> SUCCESS ", )
if status == "stacking":
    data[1]="status=SUCCESS\n"
    with open(fileName, 'w') as file:
        file.writelines(data)
        status='success'

print("status was set, status ", status)


# Automat.MouseMove(744, 664)


# Automat.WinClose(Handle)
