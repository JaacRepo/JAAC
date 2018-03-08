package examples;

import abs.api.cwi.ABSFuture;
import abs.api.cwi.LocalActor;

class NumberGenerator extends LocalActor {

    ABSFuture<Integer> getX(){
        return ABSFuture.done(5);
    }

    ABSFuture<Integer> getY(){
        return ABSFuture.done(3);
    }
}

public class Calculator extends LocalActor {

    ABSFuture<Integer> sumCoroutine(){
        NumberGenerator e = new NumberGenerator();

        ABSFuture<Integer> fx = e.send(()->e.getX());

        return getSpawn(fx,(x)->{ //first suspension point until fx bcomes available

            System.out.println("x = "+x); //first implicit resume point when x has the value computed in fx.
            ABSFuture<Integer> fy = e.send(()->e.getY());

            return getSpawn(fy, (y)->{ //second suspension point until fy becomes available

                System.out.println("y = "+y);//second implicit resume point when x has the value computed in fx.
                return ABSFuture.done(x+y);
            });
        });
}   }