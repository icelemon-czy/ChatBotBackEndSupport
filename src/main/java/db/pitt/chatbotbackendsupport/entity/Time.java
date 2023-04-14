package db.pitt.chatbotbackendsupport.entity;

public class Time{

    /**
     *  flag = -1 : invalid Time
     *
     *  Assume 23 hours later is maximum time duration.
     *  And we only use x hour or x minutes ( no combination of hour and minute allow)
     *
     *  flag = 1 : number (0-99)
     *
     *  flag = 2 : specific time (12 hour system with Ambiguity )
     *  flag = 3 : specific time (24 hour system without Ambiguity)
     *
     *  flag = 4 : duration of time
     *
     *  flag = 5 : ampmFlag
     *
     *  flag = 6 : ampmIntentionFlag
     */
    public int flag;

    public Time(int flag) {
        this.flag = flag;
    }

    public Integer number;

    public Integer hour;
    public Integer minute;

    // Assume hour:minute comes after hour2:minute2

    public Integer hour2;
    public Integer minute2;

    public Integer durationHour;
    public Integer durationMin;


    /**
     *  1: am
     *  2: pm
     */
    public Integer ampmFlag;

    @Override
    public String toString() {
        if(flag == -1){
            return "Invalid Time";
        }
        if(flag == 1){
            return "Number " + number;
        }
        if(flag == 2){
            return "Ambiguity Time "+ hour+":"+minute +" or "+ hour2 +":"+minute2;
        }
        if(flag == 3){
            return "No Ambiguity Time "+ hour+":"+minute;
        }
        if(flag == 4){
            return "Duration of Time "+ durationHour+" H,"+durationMin+" M";
        }
        if(flag == 5){
            if(ampmFlag == 1){
                return "AM";
            }else{
                return "PM";
            }
        }
        if(flag == 6){
            if(ampmFlag == 1){
                return "AM Intention";
            }else{
                return "PM Intention";
            }
        }
        return super.toString();
    }
}
