package elC;
/**
 * @(#)LoggerController: LcMessage.java
 * 
 *
 * @author Bryan Hayward
 * @version 1.00 2018/02/20
 */
public class LcMessage
{
    private LcSolarReading rd;
    
    public LcMessage(LcSolarReading rd){ this.rd=rd; }
    public LcSolarReading getReading() { return rd; }
}