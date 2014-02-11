package sha256unroll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author chabapok
 */
public class Utils {
    
    static char not(char v){
        if (v=='*') return '*';
        if (v=='1') return '0';
        return '1';
    }
    
    //"a" может превращаться в "b"
    static boolean mayConverted(char a, char b) {
        return a==b || (a=='*');
    }
    
    static Collection<String> combineNotConflicted(Collection<String> aArr, Collection<String> bArr){
        ConsolidateSet result = new ConsolidateSet( aArr.size() + bArr.size() );
        
        for(String aVariant:aArr ){
            for(String bVariant:bArr ){
                String combined = combine(aVariant, bVariant);
                if (combined!=null) result.consolidate(combined);
            }    
        }
        
        return result;
    }

    /**
     * 
     * Уточнение.
     *  0*, *0, 00 -> 0
     *  1*, *1, 11 -> 1
     *  ** - >*
     * 01,10 - запрещено
     * 
     * @param aVariant
     * @param bVariant
     * @return 
     */
    static String combine(String aVariant, String bVariant) {
        if (aVariant.length()!=bVariant.length()) throw new RuntimeException("Length not same! "+aVariant+":"+bVariant);
        
        StringBuilder sb = new StringBuilder(aVariant.length());
        
        for(int i=0; i<aVariant.length(); i++){
            char a = aVariant.charAt(i);
            char b = bVariant.charAt(i);
            
            if (a=='*') {sb.append(b); continue;}
            if (b=='*') {sb.append(a); continue;}
            if (a==b) {sb.append(a); continue;}
            return null; //01 or 10
        }
        return sb.toString();
    }
    
    
    static Collection<String> removeDupes(Collection<String> aArr, Collection<String> bArr){
        ConsolidateSet result = new ConsolidateSet( aArr.size()+bArr.size() );
        result.consolidate(aArr);
        result.consolidate(bArr);        
        return result;
    }

    
    static Node op(char op, Node ... args){
        
        Node n2 = new Node(op, args[0], args[1]);
        switch(args.length){
            case 2: return n2;
            case 3: return new Node(op, n2, args[2]);
            default: {
                    Node a[] = Arrays.copyOfRange(args, 2, args.length);
                    Node otherNodes = op(op, a);
                    Node r = new Node(op, n2, otherNodes);
                    return r;
                }
        }
    }
    
    static Node or(Node ... args){  return op('+', args); }
    static Node and(Node ... args){ return op('*', args); }
    static Node xor(Node ... args){ return op('^', args); }
    static Node not(Node arg){      return new Node('!', arg); }
    
 
    static Bits32 and(Bits32 a, Bits32 b){return a.and(b);}
    static Bits32 or(Bits32 a, Bits32 b){return a.or(b);}
    static Bits32 xor(Bits32 a, Bits32 b){return a.xor(b);}
    static Bits32 not(Bits32 a){return a.not();}

    
    static Bits32 add(Bits32 v1, Bits32 v2){ return v1.add(v2);}

    
    
    private static Node xNodes[] = new Node[1024];
    static Node x(int i){
        if (xNodes[i]==null){
            xNodes[i] =new EndNode(i);
        }
        return xNodes[i];
    }
    
    
    
    static Node[] consolidate(Bits8[] arr){
        Node[] result = new Node[arr.length*8];
        for(int i=0; i<arr.length; i++){
            System.arraycopy(arr[i].nodes, 0, result, i*8, 8);
        }
        return result;
    }
    
    
    static byte[] fromHexString(String s){
        if ( (s.length()&1) != 0) throw new RuntimeException("wrong string length");
        byte[] result = new byte[s.length()/2];
        for(int i=0, j=0; i<s.length(); i+=2, j++){
            String v = s.substring(i, i+2);
            int b = Integer.parseInt(v, 16);
            result[j] = (byte) b;
        }
        return result;
    }
    
    static char[] getBitset(byte[] arr){
        
        char[] result = new char[arr.length*8];
        
        for(int i=0; i<arr.length; i++){
            byte a = arr[i];
            for(int j=0; j<8; j++){
                
                char bit = ((a&1) == 0)? '0' : '1';
                result[i*8+j] = bit;
                
                a = (byte) (a>>>1);
            }
        }
        return result;
    }
    
    static Collection<String> probeVal(char[] needValues, Bits8[] arr){
        Node[] nodes = consolidate(arr);
        if (needValues.length != nodes.length) 
            throw new RuntimeException("Wrong lengths. NeedValues.len="+needValues.length+" and arr.size="+nodes.length+"bits");
        
        
        Collection<String> results = null;
        for(int i=0; i<nodes.length; i++){
            
            System.out.printf("Probe node [%d/%d]\n", i, nodes.length );
            if (needValues[i]=='*') continue;
            Collection<String> variants = nodes[i].probeVal( needValues[i] );
            
            if (results==null){
                results = variants;
            }else{
                results = combineNotConflicted(results, variants);
            }
        }
        return results;
    }

    
    
    
    static Bits8[] createXVars(int symCount){
        Bits8[] r = new Bits8[symCount];
        
        for(int i=0; i<r.length; i++){
            r[i] = Bits8.createX();
        }
        return r;
    }
    
    
    
    static Bits8[] fromString(String v){
        byte[] symbols = v.getBytes();
        Bits8[] r = new Bits8[symbols.length];
        
        for(int i=0; i<r.length; i++){
            r[i] = Bits8.create(symbols[i]);
        }
        return r;
    }
    
    static String foromBits8(Bits8[] b){
        byte[] r = new byte[b.length];
        
        for(int i=0; i<b.length; i++){
            r[i] = b[i].toByte();
        }
        String result = new String(r);
        return result;
    }
    
    
}
