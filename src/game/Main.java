package game;

import com.sun.org.apache.xpath.internal.operations.Plus;

import java.util.*;
import static game.InOutUtils.readStringsFromInputStream;
import static game.ProcessUtils.UTF_8;

/**
 * Main samplegame class.
 */
public class Main {

    public static void main(String[] args) {
        List<String> input = readStringsFromInputStream(System.in, UTF_8);
        if(!input.isEmpty()){
            Round round = new Round(input);
            printMovingGroups(makeMove(round));
        }
        System.exit(0);
    }


    private static List<MovingGroup> makeMove(Round round) {
        List<MovingGroup> movingGroups = new ArrayList<>();
        // Место для Вашего кода.
        List<Planet> enemyPlanets = new ArrayList<Planet>();
        round.getPlanets().stream().filter(planet -> planet.getOwnerTeam() != round.getTeamId()).forEach(planet -> enemyPlanets.add(planet));
        if(round.getCurrentStep()==0) {
            List<Planet> closePlanets = new ArrayList<Planet>();
            round.getNoMansPlanets().stream().filter(planet -> round.getDistanceMap()[9* round.getTeamId()][planet.getId()]<=round.getDistanceMap()[9*(1 - round.getTeamId())][planet.getId()]).forEach(planet -> closePlanets.add(planet));
            closePlanets.stream().forEach(planet -> {
                movingGroups.add(new MovingGroup(9*round.getTeamId(), planet.getId(), planet.getPopulation() + 1));
            });
        }else{
            List<MovingGroup> attackingG = new ArrayList<MovingGroup>();
            round.getMovingGroups().stream().filter(mg -> mg.getOwnerTeam()!=round.getTeamId()).forEach(mg -> attackingG.add(mg));
            round.getOwnPlanets().stream().filter(planet -> attackingG.stream().anyMatch(mg -> mg.getTo()==planet.getId())).forEach(planet -> {
                    movingGroups.add(new MovingGroup(planet.getId()-1+2*round.getTeamId(), planet.getId(),round.getPlanets().get(planet.getId()-1+2*round.getTeamId()).getPopulation()*1/5));
                    movingGroups.add(new MovingGroup(planet.getId()-2+3*round.getTeamId(), planet.getId(),round.getPlanets().get(planet.getId()-2+3*round.getTeamId()).getPopulation()*1/5));
//                  movingGroups.add(new MovingGroup(planet.getId()-3+4*round.getTeamId(), planet.getId(),round.getPlanets().get(planet.getId()-3+4*round.getTeamId()).getPopulation()*1/5));
            });
            if(round.getPlanets().get(9*round.getTeamId()).getPopulation()>round.getPlanets().get(9*round.getTeamId()).getReproduction()*2) {
                round.getMovingGroups().stream().filter(mg -> mg.getOwnerTeam() != round.getTeamId()).forEach(mg -> {
                    round.getOwnMovingGroups().stream().filter(mg1 -> mg1.getTo() == mg.getTo()).forEach(group ->
                            movingGroups.add(new MovingGroup(round.getTeamId() * 9, group.getTo(), group.getCount() )));
                });
            }
            round.getOwnPlanets().stream().filter(planet -> planet.getPopulation()>planet.getReproduction()).forEach(planet ->{
                    movingGroups.add(new MovingGroup(planet.getId(), planet.getId()+1-2*round.getTeamId(), planet.getId()/3));
                    movingGroups.add(new MovingGroup(planet.getId(), planet.getId()+1-2*round.getTeamId(), planet.getId()/3));
                    movingGroups.add(new MovingGroup(planet.getId(), planet.getId()+1-2*round.getTeamId(), planet.getId()/3));
            });
        }
        return movingGroups;
    }

    private static void printMovingGroups(List<MovingGroup> moves) {
        System.out.println(moves.size());
        moves.forEach(move -> System.out.println(move.getFrom() + " " + move.getTo() + " " + move.getCount()));
    }

}
