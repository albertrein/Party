package br.edu.ulbra.election.party.client;

import br.edu.ulbra.election.party.output.v1.CandidateOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Service
public class CandidateClientService {

    private final CandidateClient candidateClient;

    @Autowired
    public CandidateClientService(CandidateClient candidateClient){
        this.candidateClient = candidateClient;
    }

    public List<CandidateOutput> getAll(){
        return this.candidateClient.getAll();
    }

    @FeignClient(value="candidate-service", url="http://localhost:8082")
    private interface CandidateClient {

        @GetMapping("/v1/candidate/")
        List<CandidateOutput> getAll();
    }
}
