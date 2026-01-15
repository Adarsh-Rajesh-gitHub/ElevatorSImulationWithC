import java.util.*;

class Elevator {
    int dest = -1;
    int curFloor = 0;
    ArrayList<Person> onElev = new ArrayList<>();
    Person reserved = new Person(-1,-1, 0);
    int open = 0;
    public Elevator() {}
    int distMoved = 0;
}

class Person {
    int floorOn;
    int floorOff;
    int tPressed;
    int tBoarded;
    public Person(int on, int off, int t) {
        floorOn = on;
        floorOff = off;
        tPressed = t;
    }
}

public class Sim {  

    int totDropped = 0;
    //time from button press to pickup
    int totWaitTime = 0;
    //time from button press to dropoff
    int totTime = 0;
    public static void main(String[] args) {
        for(int i = 0; i < 1; i++) {
            Sim s = new Sim();
            s.sim();
        }
    }

    public void sim() {
        //create elevators
        ArrayList<Elevator> elevators = new ArrayList<>();
        for(int i = 0; i < 8; i++) elevators.add(new Elevator());

        //store people who pressed ground floor button (floor 0)
        Queue<Person> ground = new ArrayDeque<>();
        //store people who pressed button above ground floor (floors 1-149)
        ArrayList<Person> above = new ArrayList<>();

        //start simulation
        for(int t = 0; t < 500; t++) {
            //generate number people going up and down
            int u = (int) ((4+1) * Math.random());
            int d = (int) ((4+1) * Math.random());
            for(int i = 0; i < u; i++) {
                int floorEnd = 1 + (int) ((149)* Math.random());
                ground.add(new Person(0, floorEnd, t));
            }
            for(int i = 0; i < d; i++) {
                int floorStart = 1 + (int) ((149)*Math.random());
                above.add(new Person(floorStart, 0, t));
            }

            //general structure we will operate on all elevators andd weed out the ones through
            //comparison with e.open and capacity
            //when e.open = 1, elevator is force moved

            //drop off people at the currentFloor
            for(Elevator e : elevators) {
                //we skip if e.open is not 0 as we only need to do this the first time its on a floor in its waiting period
                if(e.open != 0) continue;
                if(e.dest == e.curFloor) {
                    e.dest = -1; 
                    if(e.reserved.floorOn != -1) {
                        e.reserved.tBoarded = t;
                        e.onElev.add(e.reserved);
                        e.open = 10;
                        e.reserved = new Person(-1,-1, 0);
                    }
                }
                for(int i = 0; i < e.onElev.size(); i++) {
                    if(e.curFloor == e.onElev.get(i).floorOff) {
                        if( e.open == 0) e.open = 10;
                        totDropped++;
                        totWaitTime+=e.onElev.get(i).tBoarded - e.onElev.get(i).tPressed;
                        totTime+= t-e.onElev.get(i).tPressed;
                        e.onElev.remove(i);
                        i--;
                    }
                }
            }

            //evenly distribute the people on the bottom
            int cnt = 0;
            while(ground.size() > 0 && cnt < 8) {
                cnt = 0;
                for(Elevator e : elevators) {
                    if( ground.size() > 0 && e.curFloor == 0 && e.onElev.size() < 20 && (e.open > 0 || e.onElev.size() == 0 && e.open == 0)) {
                        if(e.open == 0) e.open = 10;
                        Person p = ground.poll();
                        p.tBoarded = t;
                        e.onElev.add(p);
                    }
                    else cnt+=1;                
                }
            }

            //all elevators coming down are recognized by having a dest of -1 and not on ground
            //all elevators going up and down
            for(Elevator e: elevators) {
                if(e.curFloor > 0) {
                    if(e.dest == -1 && e.onElev.size() < 20) {
                        for(int i = 0; i < above.size(); i++) {
                            if(above.get(i).floorOn == e.curFloor && e.onElev.size() < 20) {
                                if(e.open == 0) e.open = 10;
                                Person p = above.get(i);
                                p.tBoarded = t;
                                e.onElev.add(p);
                                above.remove(i);
                                i--;
                            }
                        }
                    }   
                }
            }
            //move all elevators not on ground floor if they didn't have an e.open
            for(Elevator e: elevators) {
                if(e.open == 0) {
                    if(e.dest == -1 && e.curFloor != 0) {
                        e.curFloor-=1; 
                        e.distMoved++;
                    }
                    else if(e.dest != -1) {
                        e.curFloor++;
                        e.distMoved++;
                    }
                }
            }
            // for (Elevator e : elevators) {
            //     if (e.dest != -1 && e.curFloor == e.dest) {
            //         e.dest = -1;
            //     }
            // }


            //all elevators on ground floor pick one dest or if not available it defaults to highest in the people they got from the ground
            //the 10 seconds of close must have also been waited, also these elevators should be incremented and raised to first floor
            for(Elevator e: elevators) {
                if (e.onElev.isEmpty()) continue;
                if(e.curFloor == 0 && e.open == 1) {
                    Person maxPerson = Collections.max(e.onElev, (p1, p2) -> Integer.compare(p1.floorOff, p2.floorOff));
                    if(above.size()!= 0) {
                        Person aboveMax = (Collections.max(above, (p1, p2) -> Integer.compare(p1.floorOn, p2.floorOn)));
                        if(maxPerson.floorOff > aboveMax.floorOn) {
                            e.dest = maxPerson.floorOff;
                            e.reserved = new Person(-1,-1, 0);
                        }
                        else {
                            e.dest = aboveMax.floorOn;
                            above.remove(aboveMax);
                            e.reserved = aboveMax;
                        }
                    }
                    else e.dest = maxPerson.floorOff;
                }
            }


            //loop that decrements wait times for doors for all elevators
            for(Elevator e: elevators) {
                if(e.open > 0) {
                    e.open--;
                }
            }
            int c = 1;
            for(Elevator e: elevators) {
                System.out.printf(
                "t=%d | elevator=%d floor=%d dest=%d open=%d load=%d reserved=%d%n",
                t, c, e.curFloor, e.dest, e.open, e.onElev.size(),
                e.reserved.floorOn
                );
                c+=1;
            }

        }
        int avgElevatorDist = 0;
        int num = 0;
        for(Elevator e : elevators) {
            avgElevatorDist+=e.distMoved;
            num++;
        }
        if(totDropped > 0) {
            avgElevatorDist = avgElevatorDist/num;
            System.out.println("Average Distance Elevator Travels: " + avgElevatorDist);
            System.out.println("Average Wait Time for Elevator to Pick Up: " + totWaitTime/totDropped);
            System.out.println("Average Time Spent From Button Press to Drop Off: " + totTime/totDropped);
            System.out.println("Served " + totDropped + " customers");
        }
    }
}
