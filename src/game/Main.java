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
        if (!input.isEmpty()) {
            Round round = new Round(input);
            printMovingGroups(makeMove(round));
        }
        System.exit(0);
    }


    private static List<MovingGroup> makeMove(Round round) {
        List<MovingGroup> movingGroups = new ArrayList<>();
        // Место для Вашего кода.
        if (round.getCurrentStep() == 0) {
            List<Planet> closePlanets = new ArrayList<Planet>();
            round.getNoMansPlanets().stream().filter(planet ->
                    round.getDistanceMap()[9 * round.getTeamId()][planet.getId()] <= round.getDistanceMap()[9 * (1 - round.getTeamId())][planet.getId()])
                    .forEach(closePlanets::add);
            closePlanets.forEach(planet -> {
                movingGroups.add(new MovingGroup(9 * round.getTeamId(), planet.getId(), planet.getPopulation() + 1));
            });
        } else {
            List<Planet> ownPlanetsByRange = new ArrayList<>(round.getOwnPlanets());
            List<MovingGroup> attackingG = new ArrayList<MovingGroup>();
            round.getMovingGroups().stream().filter(mg -> mg.getOwnerTeam() != round.getTeamId()).forEach(attackingG::add);
            attackingG.sort(Comparator.comparing(MovingGroup::getStepsLeft));

            List<Integer> attackedPlanets = new ArrayList<>();
            attackingG.forEach(ag ->
            {
                if (!attackedPlanets.contains(ag.getTo())
                        && round.getPlanets().get(ag.getTo()).getOwnerTeam() == round.getTeamId()
                        && ag.getStepsLeft() == round.getDistanceMap()[ag.getFrom()][ag.getTo()]) {
                    attackedPlanets.add(ag.getTo());
                }
            });
            attackedPlanets.sort(Comparator.comparing(ap -> round.getPlanets().get(ap).getReproduction()));
            Collections.reverse(attackedPlanets);
            round.getPlanets().stream().filter(planet -> attackedPlanets.contains(planet.getId())).forEach(planet ->
            {
                int sum = attackingG.stream().filter(ag -> ag.getTo() == planet.getId() && ag.getStepsLeft() == round.getDistanceMap()[ag.getFrom()][ag.getTo()])
                        .mapToInt(MovingGroup::getCount).sum();
                ownPlanetsByRange.sort(Comparator.comparing(planet1 -> round.getDistanceMap()[planet1.getId()][planet.getId()]));
                if (ownPlanetsByRange.size() > 2) {
                    for (int i = 0; i < 3; i++)
                        if (!attackedPlanets.contains(ownPlanetsByRange.get(i).getId()))
                            movingGroups.add(new MovingGroup(ownPlanetsByRange.get(i).getId(), planet.getId(), sum / 3));
                } else if (ownPlanetsByRange.size() == 2) {
                    for (int i = 0; i < 2; i++) {
                        if (!attackedPlanets.contains(ownPlanetsByRange.get(i).getId()))
                            movingGroups.add(new MovingGroup(ownPlanetsByRange.get(i).getId(), planet.getId(), sum / 2));
                    }
                } else if (ownPlanetsByRange.size() == 1) {
                    if (!attackedPlanets.contains(ownPlanetsByRange.get(0).getId()))
                        movingGroups.add(new MovingGroup(ownPlanetsByRange.get(0).getId(), planet.getId(), sum));
                }
            });
            round.getMovingGroups().stream().filter(mg -> mg.getOwnerTeam() != round.getTeamId()).forEach(mg -> {
                round.getOwnMovingGroups().stream()
                        .filter(mg1 -> mg1.getTo() == mg.getTo() && mg.getStepsLeft() == round.getDistanceMap()[mg.getFrom()][mg.getTo()])
                        .forEach(group ->
                        {
                            ownPlanetsByRange.sort(Comparator.comparing(planet -> round.getDistanceMap()[planet.getId()][group.getTo()]));
                            movingGroups.add(new MovingGroup(round.getTeamId() * 9, group.getTo(), group.getCount()));
                        });
            });
            List<Planet> enemyPlanets = new ArrayList<>();
            round.getPlanets().stream().filter(planet ->
                    planet.getOwnerTeam() != round.getTeamId() && planet.getOwnerTeam() != -1)
                    .forEach(enemyPlanets::add);
            if (round.getCurrentStep() > 8) {
                round.getNoMansPlanets().forEach(planet -> {
                    ownPlanetsByRange.sort(Comparator.comparing(planet1 -> round.getDistanceMap()[planet1.getId()][planet.getId()]));
                    movingGroups.add(new MovingGroup(ownPlanetsByRange.get(0).getId(), planet.getId(), ownPlanetsByRange.get(0).getReproduction()-1));
                });
            }
            if ( enemyPlanets.size()!=0 && round.getOwnMovingGroups().stream().noneMatch(group -> round.getPlanets().get(group.getTo()).getOwnerTeam() ==( 1 - round.getTeamId()) && group.getCount()>1)
                    && attackedPlanets.size()==0) {
                List<Planet> enemyPlanetsByQuantity = new ArrayList<>(enemyPlanets);
                enemyPlanetsByQuantity.sort(Comparator.comparing(planet ->
                        round.getOwnPlanets().stream().filter(oPlanet -> round.getDistanceMap()[oPlanet.getId()][planet.getId()] <= 8).count()));
                Collections.reverse(enemyPlanetsByQuantity);
                ownPlanetsByRange.sort(Comparator.comparing(planet ->
                        round.getDistanceMap()[planet.getId()][enemyPlanetsByQuantity.get(0).getId()]));
                int sum = 0;
                for (int i = 0; i < ownPlanetsByRange.size() && round.getDistanceMap()[ownPlanetsByRange.get(i).getId()][enemyPlanetsByQuantity.get(0).getId()] <= 8; i++)
                    ++sum;
                boolean f=false;
                for (int i = 0; i < ownPlanetsByRange.size() && round.getDistanceMap()[ownPlanetsByRange.get(i).getId()][enemyPlanetsByQuantity.get(0).getId()] <= 8; i++)
                    movingGroups
                            .add(new MovingGroup(ownPlanetsByRange.get(i).getId(), enemyPlanetsByQuantity.get(0).getId(),
                                    (enemyPlanetsByQuantity.get(0).getPopulation() + enemyPlanetsByQuantity.get(0).getReproduction() * (round
                                            .getDistanceMap()[ownPlanetsByRange.get(i).getId()][enemyPlanetsByQuantity.get(0).getId()] + 2) / sum)));
            }
        }
        if ((long) movingGroups.size() == 0 && round.getOwnPlanets().size()>1)
            movingGroups.add(new MovingGroup(round.getOwnPlanets().get(0).getId(), round.getOwnPlanets().get(1).getId(), 1));
        return movingGroups;
    }

    private static void printMovingGroups(List<MovingGroup> moves) {
        System.out.println(moves.size());
        moves.forEach(move -> System.out.println(move.getFrom() + " " + move.getTo() + " " + move.getCount()));
    }

}
