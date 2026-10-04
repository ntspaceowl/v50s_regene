package dev.regene.v50s;

public class RotationPolicyTest {
    static void check(boolean condition,String message) {if(!condition)throw new AssertionError(message);}
    public static void main(String[] args) {
        RotationPolicy p=new RotationPolicy(false);
        check(!p.sample(0,9.8f),"portrait must stay unexpanded");
        check(p.sample(-9.8f,0),"landscape must expand");
        check(p.sample(0,0),"flat must retain landscape");
        check(p.sample(5,5),"diagonal must retain pose");
        check(!p.sample(0,-9.8f),"portrait must release expansion");
        check(!p.sample(0,0),"flat must retain portrait");
        check(p.sample(9.8f,0),"opposite landscape must be recognized");
        System.out.println("RotationPolicy tests passed");
    }
}
