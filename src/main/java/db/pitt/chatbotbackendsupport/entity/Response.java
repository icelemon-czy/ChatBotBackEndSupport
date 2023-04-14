package db.pitt.chatbotbackendsupport.entity;
/**
 * Response Code :
 * 0  - Success
 * 1  - Ambiguity  - 12 Hour System Lack of PM or AM
 * 2  - Conflict   - 13 am
 * 5  - Fail       - Extract nothing
 *
 * Based on the Arrival Time , Resolve the Departure Time Ambiguity We choose the closest departure time
 * 7  - ResolveAmbiguity  Example arrival 1 pm  arrival 11 clock -> Return 11 am
 * 8  - CannotResolveAmbiguity Example arrival 10 am  arrival 11 clock -> Either 11 am or 11 pm is not appropriate
 */
public class Response{
    public int code;
    public Time time;

    @Override
    public String toString() {
        if(code == 0){
            return "Success: " + time;
        }
        if(code == 1){
            return "Ambiguity: "+ time;
        }
        if(code == 2){
            return "Conflict";
        }
        if(code == 5){
            return "Can't extract time";
        }
        return super.toString();
    }
}