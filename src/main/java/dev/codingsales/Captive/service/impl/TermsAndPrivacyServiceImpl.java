package dev.codingsales.Captive.service.impl;
import dev.codingsales.Captive.entity.TermsAndPrivacy;
import dev.codingsales.Captive.repository.TermsAndPrivacyRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TermsAndPrivacyServiceImpl {
    @Autowired
    private TermsAndPrivacyRepository repository;
    @Transactional(readOnly = true)
    public Optional<TermsAndPrivacy> getByType(String type){
        return repository.findByType(type);
    }
    @Transactional(readOnly = true)
    public List<TermsAndPrivacy> getAll(){
        return repository.findAll();
    }
    @Transactional
    public TermsAndPrivacy saveOrUpdate(TermsAndPrivacy terms){
        Optional<TermsAndPrivacy> existing = repository.findByType(terms.getType());
        if(existing.isPresent()){
            TermsAndPrivacy entityToUpdate = existing.get();
            entityToUpdate.setContent(terms.getContent());
            entityToUpdate.setLastUpdated(LocalDateTime.now());
            return repository.save(entityToUpdate);
        }else {
            terms.setLastUpdated(LocalDateTime.now());
            return repository.save(terms);
        }
    }
    @Transactional
    public void delete (Long id){
        repository.deleteById(id);
    }
    @Transactional
    public void initializeDefaultTerms(){
        if (repository.findByType("TERMS_OF_USE").isEmpty()){
            repository.save(new TermsAndPrivacy("TERMS_OF_USE","Estes são os termos de uso padrão. Ao utilizar o serviço, você concorda com eles."));
        }
        if (repository.findByType("PRIVACY_POLICY").isEmpty()){
            repository.save(new TermsAndPrivacy("PRIVACY_POLICY","Esta é a política de privacidade padrão. Coletamos dados para melhorar sua experiência."));
        }
    }
}
