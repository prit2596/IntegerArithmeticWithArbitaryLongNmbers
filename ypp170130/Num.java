package ypp170130;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Stack;

/**
 *     Team No: 39
 *     @author Pranita Hatte: prh170230
 *     @author Prit Thakkar: pvt170000
 *     @author Shivani Thakkar: sdt170030
 *     @author Yash Pradhan: ypp170130
 *     Long Project 1: Num - To Perform Integer arithmetic with arbitrarily large numbers
 *
 *     Updates from Previous Submission 1:
 *     >> Updated comments
 *     >> Changed default base to 1000000000
 *     >> Modified toString(), used StringBuilder
 *     >> Modified product(a,b) to use quadraticMultiplication instead of Karatsuba Multiplication
 */

public class Num implements Comparable<Num>{

    static long defaultBase = 1000000000;
    long base = defaultBase;
    long[] arr;  // array to store arbitrarily large integers
    boolean isNegative;  // boolean flag to represent negative numbers
    int len;  // actual number of elements of array that are used;  number is stored in arr[0..len-1]

    //creating HashSet of operators for quickly checking, given a string is it an operator?
    //used in evaluation of infix and postfix expressions
    static String[] arrayOperators = {"*", "+", "-", "/", "%", "^", "(", ")"};
    static HashSet<String> operators = new HashSet<>(Arrays.asList(arrayOperators));

    //default constructor
    public Num(){
    }

    /**
     * String constuctor for Num
     * @param s the string representing a number in base 10
     */
    public Num(String s) {
        int i=0;
        if(s.charAt(0)=='-'){
            isNegative=true;
            this.arr=new long[s.length()-1];
            i=1;
        }
        else {
            this.arr=new long[s.length()];
            arr[0]=s.charAt(0);
        }
        this.len=arr.length;
        this.base=10;

        for(int j=0;(s.length()-j-1)>=i; j++){
            arr[j]=Long.parseLong(""+s.charAt(s.length()-j-1));
        }

        Num n = this.convertBase(defaultBase);
        this.base = n.base;
        this.arr = n.arr;
        this.len = n.len;
    }

    /**
     * long constructor for Num
     * @param x number in base 10
     * @param newBase new base
     */
    public Num(long x, long newBase){
        // TO-DO change it to use Num methods instead
        if (x < 0) {
            this.isNegative = true;
            x*= -1;
        }

        if (x == 0) {
            this.len = 1;
            this.arr = new long[1];
            this.arr[0] = 0;
            this.base = newBase;
            return;
        }

        int digits = (int) Math.ceil((Math.log10(x)+1));
        len = (int) Math.ceil(((digits)/(Math.log10(newBase)) + 1));
        this.arr = new long[len];

        int index = 0;
        while(x > 0)
        {
            this.arr[index++] = x % newBase;
            x /= newBase;
        }
        this.base = newBase;
        this.stripZeros();
    }

    /**
     * long constructor for Num
     * creates the Num object representing that number in the chosen base.
     * @param x number in base 10
     */
    public Num(long x) {
        this(x, defaultBase);
    }

    /**
     * constructor for creating Num objects with provided fields
     * @param arr array representation of Num
     * @param len actual number of elements of array that are used;  number is stored in arr[0..len-1]
     * @param isNegative flag to represent negative numbers
     * @param base base of the Num object being created
     * */
    private Num(long[] arr, int len, boolean isNegative, long base){
        this.arr = arr;
        this.len = len;
        this.isNegative = isNegative;
        this.base = base;
    }

    /**
     * creates state, toggles the sign flags so as to perform unsigned operations
     * the operation before returning result calls unveilState to restore the input parameters
     *
     * depending upon the isNegative flag of a and b, current state is decided
     *
     *         a    b    state
     *         +    +    0
     *         +    -    1
     *         -    +    2
     *         -    -    3
     * also the sign flags of these variables are modified so as to perform unsigned operation
     * and the parameters are restored to original state before returning result
     *
     * @param a
     * @param b
     * @return state as described above
     *
     * */
    public static int createState(Num a, Num b){
        int state;
        if(!a.isNegative && !b.isNegative){
            state = 0;
        }
        else if(!a.isNegative && b.isNegative){
            state = 1;
            b.isNegative = false;
        }
        else if(a.isNegative && !b.isNegative){
            state = 2;
            a.isNegative = false;
        }
        else{
            state = 3;
            a.isNegative = false;
            b.isNegative = false;
        }
        return state;
    }

    /**
     * to restore the parameters to the original state
     * @param a
     * @param b
     * @param state as described above
     */
    public static void unveilState(Num a, Num b, int state){

        switch(state){
            case 0:
                break;
            case 1:
                b.isNegative = true;
                break;
            case 2:
                a.isNegative = true;
                break;
            case 3:
                a.isNegative = true;
                b.isNegative = true;
                break;
            default:
                break;
        }

    }

    /**
     * Performs addition of a and b
     * @param a
     * @param b
     * @return Num returns a + b
     * @throws BaseMismatchException if base of a and b are not compatible
     * */
    public static Num add(Num a, Num b) {

        if(a.base() != b.base()){
            throw new BaseMismatchException("In add: base mismatch");
        }


        Boolean isNeg = false;
        Num result;
        long currentBase = a.base();
        int currentState = createState(a, b);

        if(currentState == 1){
            result = subtract(a, b);
            unveilState(a, b, currentState);
            return result;
        }else if(currentState == 2){
            result = subtract(b, a);
            unveilState(a,b, currentState);
            return result;
        }

        int len1=a.len, len2=b.len;
        int max=len1>len2?len1:len2;
        long ans[] = new long[max+1];
        long sum,carry=0;
        int i=0;

        while (len1!=0 && len2!=0){
            sum=a.arr[i]+b.arr[i]+carry;
            carry=sum/currentBase;
            sum=sum%currentBase;
            ans[i++] = sum;
            len1--;
            len2--;
        }
        while (len1>0){
            sum=a.arr[i]+carry;
            carry=sum/currentBase;
            sum=sum%currentBase;
            len1--;
            ans[i++] = sum;
        }
        while (len2>0){
            sum=b.arr[i]+carry;
            carry=sum/currentBase;
            sum=sum%currentBase;
            len2--;
            ans[i++]=sum;
        }
        if(carry>0){
            ans[i++]=carry;
        }
        unveilState(a,b,currentState);

        //if both number is negative addition of both would be negative
        if(currentState == 3){
            isNeg = true;
        }

        result = new Num(ans, ans.length, isNeg, currentBase);
        result.stripZeros();
        return result;
    }

    /**
     * Performs subtraction: a - b
     * Uses complement for subtraction
     * @param a
     * @param b
     * @return a-b
     * @throws BaseMismatchException if base of a and b are not compatible
     */
    public static Num subtract(Num a, Num b) {

        if(a.base() != b.base()){
            throw new BaseMismatchException("In subtract: base mismatch");
        }

        Num ans;
        int currentState = createState(a, b);

        if(currentState == 1){
            ans = add(a, b);
            unveilState(a,b, currentState);
            ans.stripZeros();
            return ans;
        }else if(currentState == 2){
            ans = add(a, b);
            ans.isNegative = true;
            unveilState(a,b, currentState);
            ans.stripZeros();
            return ans;
        }
        else if(currentState == 3){
            ans = subtract(b,a);
            unveilState(a,b, currentState);
            ans.stripZeros();
            return ans;
        }

        Num shortNum = new Num(0, a.base());
        int len1=a.len;
        int len2=b.len;

        // for adding trailing zeros in b
        if(len2<len1){
            shortNum.arr=new long[len1];
            shortNum.len=a.len;
            for(int j=0; j<len2; j++){
                shortNum.arr[j] = b.arr[j];
            }
        }
        else{
            shortNum = b;
        }

        Num complement = findComplement(shortNum);
        ans=add(a,complement);
        ans.stripZeros();

        if(ans.len>(a.len > b.len ? a.len: b.len)){
            ans.len--;
        }
        else{
            ans=findComplement(ans);
            ans.isNegative=true;
        }
        unveilState(a,b, currentState);
        ans.stripZeros();
        return ans;
    }

    /**
     *
     * @param c
     * @return complement of c
     */
    private static Num findComplement(Num c){
        long currentBase = c.base();
        Num one=new Num(1, currentBase);
        Num d = new Num(new long[c.len], c.len, c.isNegative, currentBase);
        Num result;

        for(int i=0; i<c.len; i++){
            d.arr[i]=(currentBase-1)-c.arr[i];
        }
        result = add(d, one);
        result.setBase(currentBase);
        return result;
    }

    /**
     *
     * @param base sets the base to the base specified as a parameter
     */
    private void setBase(long base){
        this.base = base;
    }

    /**
     * helper functions for product using karatsuba multiplication
     * @param a
     * @param len
     * @return Num instance with length same as len
     */
    private static Num equalLength(Num a, int len){
        long arr[] = new long[len];
        for(int i = 0;i<a.len;i++){
            arr[i] = a.arr[i];
        }
        return new Num(arr, len, a.isNegative, a.base);
    }

    /**
     * performs shift operation
     * helper function for using in product
     * @param a
     * @param n indicates how much to shift left
     * @return left shifted Num instance
     */
    public static Num shift(Num a, int n){
        long[] arr = new long[a.len + n];
        for(int i = n; i<arr.length; i++){
            arr[i] = a.arr[i-n];
        }
        return new Num(arr, arr.length, a.isNegative, a.base);
    }

    /**
     * Computes product: a*b using karatsuba multiplication algorithm
     * @param a
     * @param b
     * @return a*b
     * @throws BaseMismatchException if base of a and b are not compatible
     */
    public  static Num karatsubaMultiplication(Num a, Num b){

        if(a.base() != b.base()){
            throw new BaseMismatchException("In product: base mismatch");
        }

        int currentState = createState(a, b);

        long currentBase = a.base();
        Num x, y;
        int total_length;
        //check for same length; if not add length
        if(a.len > b.len){
            y = equalLength(b, a.len);
            x = a;
            total_length = a.len;
        }
        else if(a.len < b.len){
            x = equalLength(a, b.len);
            y = b;
            total_length = b.len;
        }
        else{
            x = a;
            y = b;
            total_length = a.len;
        }

        //base case: karatsuba: len = 1
        if(total_length == 1){
            long sum = x.arr[0] * y.arr[0];
            long carry = sum/currentBase;
            sum = sum%currentBase;
            long[] arr_result;
            if(carry > 0){
                arr_result = new long[2];
                arr_result[1] = carry;
            }
            else {
                arr_result = new long[1];
            }
            arr_result[0] = sum;
            unveilState(a, b, currentState);

            //sign of result is -ve if either of the parameters is negative
            if((!a.isNegative && b.isNegative)||(a.isNegative && !b.isNegative))
            {
                x.isNegative = true;
            }
            return new Num(arr_result, arr_result.length, x.isNegative, x.base);
        }


        //divide and conquer: divide step
        int halfLength = x.len/2;

        long[] arr_xl, arr_xh, arr_yl, arr_yh;

        arr_xl = new long[halfLength];
        arr_xh = new long[total_length - halfLength];
        arr_yl = new long[halfLength];
        arr_yh = new long[total_length - halfLength];

        for(int i = 0; i<total_length; i++){
            if(i<halfLength){
                arr_xl[i] = x.arr[i];
                arr_yl[i] = y.arr[i];
            }
            else{
                arr_xh[i-halfLength] = x.arr[i];
                arr_yh[i-halfLength] = y.arr[i];
            }
        }

        //divide and conquer: conquer step
        Num xl, xh, yl, yh;
        xl = new Num(arr_xl, halfLength, x.isNegative, x.base);
        xh = new Num(arr_xh, total_length - halfLength, x.isNegative, x.base);
        yl = new Num(arr_yl, halfLength, y.isNegative, y.base);
        yh = new Num(arr_yh, total_length - halfLength, y.isNegative, y.base);

        Num p1, p2, p3, a1, a2;
        p1 = karatsubaMultiplication(xh, yh);
        p3 = karatsubaMultiplication(xl, yl);
        p2 = subtract(subtract(karatsubaMultiplication(add(xl,xh), add(yl, yh)), p1), p3);


        //shift the xh*yh with twice of halflength
        a1 = shift(p1, halfLength * 2);
        a2 = shift(p2, halfLength);

        // adding result = a1 + a2 + p3;
        Num result = add(a1,add(a2, p3));

        unveilState(a, b, currentState);
        if(currentState==1 || currentState==2){
            result.isNegative = true;
        }
        return result;
    }

    /**
     * Performs multiplication a*b using traditional quadratic method
     * @param a
     * @param b
     * @return a*b
     */
    public static Num quadraticMultiplication(Num a, Num b) {

        if(a.base() != b.base()){
            System.out.println("Base mismatch error");
            return null;
        }

        long currentBase = a.base();
        long arr[] = new long[a.len+b.len];

        Num product = new Num();
        product.base = currentBase;
        long carry = 0;
        long total = 0;
        long sum = 0;
        int i = 0, j = 0;
        int last = 0;
        for(i = 0; i < a.len; i++){
            for(j = 0; j < b.len; j++){

                total = carry + arr[i+j] + a.arr[i] * b.arr[j];
                sum = total%currentBase;
                arr[i+j] = sum;
                last = i+j;
                carry = total/currentBase;
            }
            arr[last+1] += carry;
            carry = 0;
        }

        if((!a.isNegative && b.isNegative)||(a.isNegative && !b.isNegative)){
            product.isNegative = true;
        }

        product.arr = arr;
        product.len = a.len+b.len;

        product.stripZeros();

        return product;
    }

    /**
     * Calls quadratic multiplication internally
     * @param a
     * @param b
     * @return a*b
     */
    public static Num product(Num a, Num b) {
        return quadraticMultiplication(a,b);
    }


    /**
     * Internally calls power(Num a, Num n)
     * Precondition: a > 1, n >=0
     * @param a
     * @param n
     * @return a^n
     */
    public static Num power(Num a, long n) {
        return power(a, new Num(n, a.base()));
    }

    /**
     * computes a^n using divide and conquer
     * @param a
     * @param n
     * @return a^n
     */
    private static Num power(Num a, Num n){

        long currentBase = a.base();

        //base case: n = 0
        if(n.compareTo(new Num(0, currentBase)) == 0){
            return new Num(1, currentBase);
        }

        Num ans = new Num();
        ans.setBase(currentBase);
        ans = power(product(a, a), n.by2());


        if(mod(n, new Num(2, currentBase)).compareTo(new Num(0, currentBase)) == 0){
            // n is even
            return ans;
        }
        else
        {
            // n is odd
            return product(ans, a);
        }
    }

    /**
     * Uses binary search to calculate a/b
     * Computes Integer division a/b
     * @param a
     * @param b
     * @return a/b
     * @throws BaseMismatchException if base of a and b are not compatible
     */
    public static Num divide(Num a, Num b) {

        if(a.base() != b.base()){
            throw new BaseMismatchException("In divide: Base Mismatch");
        }

        long currentBase = a.base();
        Num zero = new Num(0, currentBase);

        if(b.compareTo(zero) == 0){
            return null;
        }

        int currentState = createState(a,b);
        Num low, high, mid;

        low = new Num(1, currentBase);
        high = a;

        Num threshold = subtract(a, b);
        Num product;
        Num minusOne = new Num(-1, currentBase);
        Num one = new Num(1, currentBase);

        //repeat until convergence
        while(true){
            mid = add(low, high).by2();
            product = product(mid, b);
            if(product.compareTo(a) == 0){
                break;
            }

            if(product.compareTo(threshold) == 1 && product.compareTo(a) != 1){
                break;
            }

            if(product.compareTo(a) == 1){
                high = add(mid, minusOne);
            }else{
                low = add(mid, one);
            }
        }

        unveilState(a, b, currentState);

        if((!a.isNegative && b.isNegative)||(a.isNegative && !b.isNegative)){

            mid.isNegative = true;
            if(product(mid, b).compareTo(a) != 0){
                mid = subtract(mid, one);
            }
        }
        return mid;
    }

    /**
     * Computes a mod b
     * @param a
     * @param b
     * @return a%b
     * @throws BaseMismatchException if base of a and b are not compatible
     */
    public static Num mod(Num a, Num b) {

        if(a.base() != b.base()){
            throw new BaseMismatchException("In mod: Base Mismatch");
        }

        long currentBase = a.base();
        Num zero = new Num(0, currentBase);

        if(b.compareTo(zero) == 0){
            return null;
        }

        Num q = divide(a, b);
        return subtract(a, product(b, q));
    }

    /**
     * Computes square root of a, using binary search
     * precondition: a is non negative
     * @param a
     * @return a^(0.5)
     */
    public static Num squareRoot(Num a) {

        long currentBase = a.base();
        Num zero = new Num(0, currentBase);

        if(a.compareTo(zero) == -1){
            return null;
        }

        Num low, high, mid, midSq;

        low = zero;
        high = a;
        int i = 0;
        while(true){

            mid = add(low, high).by2();

            midSq = power(mid, 2);

            if(midSq.compareTo(a) == 0)
            {
                return mid;
            }

            if(mid.compareTo(low) == 0 || mid.compareTo(high) == 0){
                return mid;
            }

            if(midSq.compareTo(a) == 1){
                high = mid;
            }
            else{
                low = mid;
            }
        }
    }



    // Utility functions
    /**
     *
     * Compares this with other, both this and other are Num instances
     * @param other Num instance being compared
     * @return +1 if this is greater, 0 if equal, -1 otherwise
     * @throws BaseMismatchException if base of a and b are not compatible
     */
    public int compareTo(Num other) {

        if(this.base() != other.base()){
            throw new BaseMismatchException("In compareTo: Base Mismatch");
        }

        Num difference1 = subtract(this, other);
        Num difference2 = subtract(other,this);

        if(!difference1.isNegative && !difference2.isNegative){
            return 0;
        }else if(difference1.isNegative){
            return -1;
        }
        return 1;
    }

    // Output using the format "base: elements of list ..."
    // For example, if base=100, and the number stored corresponds to 10965,
    // then the output is "100: 65 9 1"

    /**
     * Output using the format "base: elements of list ..."
     * For example, if base=100, and the number stored corresponds to 10965,
     * then the output is "100: 65 9 1"
     */
    public void printList() {
        System.out.print( ""+this.base() + ": ");
        if(this.isNegative == true){
            System.out.print("- ");
        }

        for(int i = 0; i < len; i++){
            System.out.print(arr[i]+" ");
        }
        System.out.println();
    }

    /**
     *
     * @return decimal represention of the Num instance invoking it
     */
    public String toString() {
        Num n;
        if(this.base() == defaultBase){
            n = this;
        }
        else{
            n = this.convertBase(defaultBase);
        }

        int i;
        StringBuilder result = new StringBuilder();
        int baseLength = String.valueOf(defaultBase).length() - 1;
        int pad; //number of zeros to pad before appending to string
        if(this.isNegative){
            result.append("-");
        }
        i = n.len - 1;
        result.append(n.arr[i]);
        for(i = i - 1; i >=0; i--){
            pad = baseLength - String.valueOf(n.arr[i]).length();
            while(pad>0){
                result.append("0");
                pad--;
            }
            result.append(n.arr[i]);
        }
        return result.toString();
    }


    /**
     *
     * @return base of the Num object
     */
    public long base() { return base; }


    /**
     * Return number equal to "this" number, in base=newBase
     * @param newBase
     * @return converted Num instance
     */
    public Num convertBase(long newBase) {

        Num oldBaseToNew = new Num(this.base(), newBase);
        Num sum = new Num(0, newBase);
        for(int i=this.len-1; i>=0; i--){
            sum = add(product(sum, oldBaseToNew), new Num(this.arr[i], newBase));
        }
        sum.stripZeros();
        sum.isNegative = this.isNegative;
        return sum;
    }


    /**
     * Divide by 2, for using in binary search
     * @return this/2, this is a Num instance
     */
    public Num by2() {
        long[] qArr = new long[this.len];
        int i = this.len - 1;
        long c=0;
        while(i>=0){
            qArr[i] = (c*this.base() + this.arr[i])/2;
            c = this.arr[i]%2L;
            i--;
        }

        Num q = new Num(qArr, this.len, false, this.base);
        q.stripZeros();

        if(this.isNegative){
            if(mod(this, new Num(2, this.base)).compareTo(new Num(0, this.base)) == 1){
                q = add(q, new Num(1, this.base));
            }
            q.isNegative = this.isNegative;
        }
        return q;
    }


    /**
     * utility function to trim trailing zeros
     */
    public void stripZeros(){
        while(this.len > 1 && this.arr[this.len -1] == 0){
            this.len--;
        }
        if(this.len == 1 && this.arr[0] == 0){
            this.isNegative = false;
        }
    }




    /**
     * Evaluate an expression in postfix and return resulting number
     * Each string is one of: "*", "+", "-", "/", "%", "^", "0", or
     * a number: [1-9][0-9]*.  There is no unary minus operator.
     * @param expr postfix expression
     * @return result of evaluation
     */
    public static Num evaluatePostfix(String[] expr) {
        String token="";
        Stack<Num> myStack = new Stack<>();
        Num a,b;

        for(int i = 0; i<expr.length; i++){
            token = expr[i];
            if(!isOperator(token)){
                myStack.push(new Num(token));
            }
            else{
                b = myStack.pop();
                a = myStack.pop();

                myStack.push(evaluate(a, token, b));
            }
        }

        return myStack.pop();
    }

    /**
     * helper method for using in evaluation
     * @param a
     * @param operator
     * @param b
     * @return result of operation: a operator b
     */
    public static Num evaluate(Num a, String operator, Num b){
        switch (operator){
            case "+":
                return add(a, b);
            case "-":
                return subtract(a, b);
            case "*":
                return product(a, b);
            case "/":
                return divide(a, b);
            case "%":
                return mod(a, b);
            case "^":
                return power(a, b);
            default:
                return null;
        }
    }



    /**
     * Evaluate an expression in infix and return resulting number
     * Each string is one of: "*", "+", "-", "/", "%", "^", "(", ")", "0", or
     * a number: [1-9][0-9]*.  There is no unary minus operator.
     * Converts infix expression to postfix and then calls evaluatePostfix
     * @param expr infix expression
     * @return result of infix evalutation
     */
    public static Num evaluateInfix(String[] expr) {
        ArrayList<String> postfix = new ArrayList<>();
        Stack<String> myStack = new Stack<>();
        int j = 0, precedence = -1;
        String token="";

        for(int i = 0; i < expr.length; i++){

            token = expr[i];

            if(!isOperator(token)){
                postfix.add(token);
            }
            else if(token.equals("(")){
                myStack.push(token);
            }
            else if(token.equals(")")){
                while(!myStack.peek().equals("(")){
                    postfix.add(myStack.pop());
                }
                myStack.pop();
            }
            else if(isOperator(token)){
                precedence = getPrecedence(token);

                while (!myStack.empty() && ((getPrecedence(myStack.peek()) > precedence) || ((getPrecedence(myStack.peek()) == precedence) && !isRightAssociative(token)))){
                    postfix.add(myStack.pop());
                }
                myStack.push(token);
            }
        }

        while (myStack.empty() == false){
            postfix.add(myStack.pop());
        }

        String[] postfixArray = new String[postfix.size()];

        return evaluatePostfix(postfix.toArray(postfixArray));
    }

    /**
     *
     * @param t operator
     * @return true if t is right associative, false otherwise
     */
    public static boolean isRightAssociative(String t){
        switch (t){
            case "^":
                return true;
            default:
                return false;
        }
    }

    /**
     *
     * @param t operator
     * @return int value corresponding to precedence of operator t
     */
    public static int getPrecedence(String t){

        switch (t){
            case "+":
                return 0;
            case "-":
                return 0;
            case "*":
                return 1;
            case "/":
                return 1;
            case "%":
                return 1;
            case "^":
                return 2;
            default:
                return -1;
        }
    }

    /**
     *
     * @param s
     * @return true if string s is one of following operators: "*", "+", "-", "/", "%", "^", "(" or ")"
     */
    public static boolean isOperator(String s){
        if(operators.contains(s)){
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        Num a = new Num(100);
        Num b = new Num(5);

        System.out.println("A: "+a.toString());
        System.out.println("B: "+b.toString());

        Num c = divide(a,b);
        System.out.println("Result of A/B: " + c.toString());

        Num d = product(a, b);
        System.out.println("Result of A*B: " + d.toString());

        String[] expr = {"9", "/", "3", "+", "7", "-", "(", "5", "*", "1", ")"};

        System.out.println("Infix expression: "+ "9/3+7-(5*1)");
        System.out.println("Result:"+evaluateInfix(expr));
    }
}