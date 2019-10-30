# coding=utf-8

import time, random, os, json
from os import system
from PyAutoItPy import AutoItX, WinHandle, WinState, MB_Flags

account_string = ""
sum_string = ""
note_string = ""
status_string = ""
status = ""
x = 0
y = 1


fileName = os.path.realpath('..') +  R"\payments\payment"    #'\payments\payment'

while True:
    while (os.path.exists(fileName) == False):
        print("pass")
        print("file ",fileName)
        time.sleep(2)
        pass
    try:
        with open(fileName, 'r') as json_file:
            data = json.load(json_file)
            print('number: ' + data['number'])
            print('sum: ' + data['sum'])
            print('text: ' + data['text'])
            print('status: ' + data['status'])
            print('')

            acc_list = list(data['number'].lower().strip())
            sum_string = data['sum'].lower().strip()
            status = data['status'].lower().strip()
            note_string=data['text'].lower().strip()
            note_list = list(data['text'].lower().strip())
            #input("press Enter for starting procedure...")


        '''for str in data:
            if "number" == str[0:6]:
                account_string = str
                acc_list = list(account_string)
            if "sum" == str[0:3]:
                sum_string = str
            if "text" == str[0:4]:
                note_string = str.lower().split("=")[1].strip()
                note_list = list(note_string)
            if "status" == str[0:6]:
                status_string = str
                status = status_string.lower().split("=")[1].strip()'''



        print("Set status in ACCEPTED -> LOADING " )
        if status == "accepted":
            data['status'] = "LOADING"
            status = "loading"
            with open(fileName, 'w') as file:
                json.dump(data, file)
        print("status = ", status)

        '''
        def randomValue(currV):
            cV= int(currV)

            print("text::",random.randint(cV-10,currV+10))
            return str(random.randint(cV-10,currV+10))
        '''
        strButton = {" ": "550 711",
                     "й": "140 432",  # randomValue(140)+ "+"+randomValue(432),
                     "ц": "229 432",
                     "у": "332 432",
                     "к": "410 432",
                     "е": "505 432",
                     "н": "595 432",
                     "г": "687 432",
                     "ш": "778 432",
                     "щ": "866 432",
                     "з": "959 432",

                     "ф": "186 530",
                     "ы": "275 530",
                     "в": "365 530",
                     "а": "458 530",
                     "п": "551 530",
                     "р": "641 530",
                     "о": "730 530",
                     "л": "823 530",
                     "д": "912 530",
                     "ж": "1004 530",
                     "э": "1100 530",

                     "я": "276 621",
                     "ч": "370 621",
                     "с": "460 621",
                     "м": "550 621",
                     "и": "638 621",
                     "т": "730 621",
                     "ь": "822 621",
                     "б": "911 621",
                     "ю": "1010 621",
                     ".": "760 621",
                     "ё": "503 433",
                     "х": "1051 432",
                     "ъ": "1142 432",
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

        
        al = ''
        print("acc_list: ", acc_list)
        for al in acc_list:
            print("al_letter = ", al.lower())
            if al.lower() in iButton:
                print("coordinates: ", iButton.get(al.lower()))
                print(int(iButton[al.lower()].split(" ")[0]))
                Automat.ControlClick(Title, Control, int(iButton[al.lower()].split(" ")[x]),
                                     int(iButton[al.lower()].split(" ")[y]), 1, "left", "")
            time.sleep(1)
        print("......")

        #input("press ENTER for continue...")


        '''
        page where printing avatar
        631 606
        color=0xEFEBE7
        grey
        '''
        Automat.ControlClick(Title, Control, int(iButton["Next"].split(" ")[x]), int(iButton["Next"].split(" ")[y]), 1,
                             "left",
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
        Automat.ControlClick(Title, Control, int(iButton["Confirm"].split(" ")[x]),
                             int(iButton["Confirm"].split(" ")[y]), 1,
                             "left", "")  # Verno
        print("Verno")
        time.sleep(3)

        li = ''
        print("note_list: ", note_list)
        for li in note_list:
            print("li_letter = ", li.lower())
            if li.lower() in strButton:
                print("coordinates: ", strButton.get(li.lower()))
                print(int(strButton[li.lower()].split(" ")[0]))
                Automat.ControlClick(Title, Control, int(strButton[li.lower()].split(" ")[x]),
                                     int(strButton[li.lower()].split(" ")[y]), 1, "left", "")
            time.sleep(1)
        print("......")

        #input("press ENTER for continue...")

        Automat.ControlClick(Title, Control, int(iButton["Next"].split(" ")[x]), int(iButton["Next"].split(" ")[y]), 1,
                             "left",
                             "")  # Next
        print("Next")
        time.sleep(5)

        print("Set status in LOADING -> COMPLETED ", )
        if status == "loading":
            data['status'] = "COMPLETED"
            with open(fileName, 'w') as file:
                json.dump(data, file)
                status = 'comleted'
        print("status was set, status= ", status)

        print("waiting status  STACKED... ")
        while status != "stacked":
            time.sleep(1)
            with open(fileName, 'r') as json_file:
                data = json.load(json_file)
                status = data['status'].lower().strip()
        print("status set STACKED, continue work with status= ", status)
        #input("Waiting....")
        print("press button payment...")
        time.sleep(2)
        Automat.ControlClick(Title, Control, int(iButton["Next"].split(" ")[x]), int(iButton["Next"].split(" ")[y]), 1,
                             "left",
                             "")  # Pay
        print("Main page")

        print("Set status in STACKED -> SUCCESS ", )
        if status == "stacked":
            data['status'] = "SUCCESS"
            with open(fileName, 'w') as file:
                json.dump(data, file)
                status = 'success'

        print("status was set, status ", status)
        time.sleep(3)
        Automat.ControlClick(Title, Control, int(iButton["Main"].split(" ")[x]), int(iButton["Main"].split(" ")[y]), 1,
                             "left",
                             "")  # MainPage
        time.sleep(5)
        print("Main Page Button on low-center")
    except BaseException:
        print("Unknown error")

# Automat.MouseMove(744, 664)


# Automat.WinClose(Handle)
