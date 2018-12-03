package br.edu.ulbra.election.party.repository;

import br.edu.ulbra.election.party.model.Party;
import org.springframework.data.repository.CrudRepository;

public interface PartyRepository extends CrudRepository<Party, Long> {

    Party findFirstByNumber(Integer number);
    Party findFirstByCode(String code);
    Party findByNumber(Integer number);
    Party findByCode(String code);
}
