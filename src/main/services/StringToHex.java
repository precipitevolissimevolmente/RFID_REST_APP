package main.services;

/**
 * Created by G on 18.06.2015.
 */
public class StringToHex {
    public String convertStringToHex(String str){

        char[] chars = str.toCharArray();

        StringBuffer hex = new StringBuffer();
        for(int i = 0; i < chars.length; i++){
            hex.append(Integer.toHexString((int)chars[i]));
        }

        return hex.toString();
    }

    public String convertHexToString(String hex){

        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        //49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for( int i=0; i<hex.length()-1; i+=2 ){

            //grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            //convert the decimal to character
            sb.append((char)decimal);

            temp.append(decimal);
        }
        System.out.println("Decimal : " + temp.toString());

        return sb.toString();
    }

    public static void main(String[] args) {

        StringToHex strToHex = new StringToHex();
        System.out.println("\n***** Convert ASCII to Hex *****");
        String str = "I Love Java!";
        System.out.println("Original input : " + str);

        String hex = strToHex.convertStringToHex(str);

        System.out.println("Hex : " + hex);

        System.out.println("\n***** Convert Hex to ASCII *****");
        System.out.println("Hex : " + hex);
        System.out.println("ASCII : " + strToHex.convertHexToString(hex));
        System.out.println("ASCII : " + strToHex.convertHexToString("04B9C3F6122726809348F001FFFFFFFF26837BA4A683B90726C3B92726C3B9272D88EC57343A9BF5DFE17145229309271AC3B83726C3B92726C3B93700014D66"));
    }
}
