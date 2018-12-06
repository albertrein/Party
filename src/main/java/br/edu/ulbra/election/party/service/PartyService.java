package br.edu.ulbra.election.party.service;

import br.edu.ulbra.election.party.client.CandidateClientService;
import br.edu.ulbra.election.party.exception.GenericOutputException;
import br.edu.ulbra.election.party.input.v1.PartyInput;
import br.edu.ulbra.election.party.model.Party;
import br.edu.ulbra.election.party.output.v1.CandidateOutput;
import br.edu.ulbra.election.party.output.v1.GenericOutput;
import br.edu.ulbra.election.party.output.v1.PartyOutput;
import br.edu.ulbra.election.party.repository.PartyRepository;
import feign.FeignException;
import org.apache.commons.lang.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class PartyService {
    private CandidateClientService candidateClientService;
    private final PartyRepository partyRepository;

    private final ModelMapper modelMapper;

    private static final String MESSAGE_INVALID_ID = "Invalid id";
    private static final String MESSAGE_PARTY_NOT_FOUND = "Party not found";

    @Autowired
    public PartyService(PartyRepository partyRepository,CandidateClientService candidateClientService, ModelMapper modelMapper){
        this.partyRepository = partyRepository;
        this.candidateClientService = candidateClientService;
        this.modelMapper = modelMapper;
    }

    public List<PartyOutput> getAll(){
        List<Party> partyList = (List<Party>)partyRepository.findAll();
        return partyList.stream().map(Party::toPartyOutput).collect(Collectors.toList());
    }

    public PartyOutput create(PartyInput partyInput) {
        validateInput(partyInput,false);
        validateDuplicate(partyInput, null);
        Party party = modelMapper.map(partyInput, Party.class);
        party = partyRepository.save(party);
        return Party.toPartyOutput(party);
    }

    public PartyOutput getById(Long partyId){
        if (partyId == null){
            throw new GenericOutputException(MESSAGE_INVALID_ID);
        }

        Party party = partyRepository.findById(partyId).orElse(null);
        if (party == null){
            throw new GenericOutputException(MESSAGE_PARTY_NOT_FOUND);
        }

        return modelMapper.map(party, PartyOutput.class);
    }

    public PartyOutput update(Long partyId, PartyInput partyInput) {
        if (partyId == null){
            throw new GenericOutputException(MESSAGE_INVALID_ID);
        }
        validateInput(partyInput,true);
        validateDuplicate(partyInput, partyId);

        Party party = partyRepository.findById(partyId).orElse(null);
        if (party == null){
            throw new GenericOutputException(MESSAGE_PARTY_NOT_FOUND);
        }

        party.setCode(partyInput.getCode());
        party.setName(partyInput.getName());
        party.setNumber(partyInput.getNumber());
        party = partyRepository.save(party);
        return modelMapper.map(party, PartyOutput.class);
    }

    //Check if there are any candidates still on the party
    public void verifyPartyCandidates(Long id){
        try {
            List<CandidateOutput> candidates = candidateClientService.getAll();
            CandidateOutput candidate;
            int i=0;
            for(i=0;i<candidates.size();i++){
                candidate = candidates.get(i);
                if(candidate.getPartyOutput().getId()==id){
                    throw new GenericOutputException("The Party has candidates yet");
                }
            }
        } catch(FeignException e){
            if(e.status() == 500){
                throw new GenericOutputException("Invalid candidate");
            }
        }
    }

    public GenericOutput delete(Long partyId) {
        if (partyId == null) {
            throw new GenericOutputException(MESSAGE_INVALID_ID);
        }

        Party party = partyRepository.findById(partyId).orElse(null);
        if (party == null) {
            throw new GenericOutputException(MESSAGE_PARTY_NOT_FOUND);
        }

        verifyPartyCandidates(partyId);

        partyRepository.delete(party);
        return new GenericOutput("Party deleted");
    }
    private void validateDuplicate(PartyInput partyInput, Long id){
        Party party = partyRepository.findFirstByCode(partyInput.getCode());
        if (party != null && !party.getId().equals(id)){
            throw new GenericOutputException("Duplicate Code");
        }
        party = partyRepository.findFirstByNumber(partyInput.getNumber());
        if (party != null && !party.getId().equals(id)){
            throw new GenericOutputException("Duplicate Number");
        }
    }

    private void validateInput(PartyInput partyInput, boolean isUpdate){
        if (StringUtils.isBlank(partyInput.getName())){
            throw new GenericOutputException("Invalid name");
        }
        if(partyInput.getName().trim().length() < 5){
            throw new GenericOutputException("Invalid Name, must contain at least 5 characters");
        }
        if (StringUtils.isBlank(partyInput.getCode())){
            throw new GenericOutputException("Invalid code");
        }
        if(partyRepository.findByCode(partyInput.getCode()) != null){
            if(!isUpdate) {
                throw new GenericOutputException("Code already exists");
            }
        }
        if (partyInput.getNumber() == null || partyInput.getNumber() < 10 || partyInput.getNumber() > 99){
            throw new GenericOutputException("Invalid number, must contain 2 characters");
        }
        if(partyRepository.findByNumber(partyInput.getNumber()) != null){
            if(!isUpdate) {
                throw new GenericOutputException("Number already exists");
            }
        }
    }

}
