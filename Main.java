import java.util.*; 
import java.io.*;
class Main 
{
    public static void main(String args[])
    {
        Scanner in = new Scanner(System.in); 
        System.out.println("Enter the number of elements to be added intially");
        int n = in.nextInt(); 
        LinkedList l = new LinkedList<>();
        LinkedList temp = new LinkedList<>();
        for(int i = 0; i < n; i++)
        {
            System.out.println("Enter "+ i+" th node = ");
            int t = in.nextInt(); 
            l.add(t);
        }  
        for(int i = n-1; i >= 0; i--)
        {
            temp.add(l.get(i));
        }
        System.out.println(temp); 
        int c = temp.size(); 
        if(c%2==0)
        {
            System.out.println(temp.get(c/2));
        }
        else 
        {
            System.out.println(temp.get((int)Math.floor((c+1)/2)-1));
        }
        
    }
}