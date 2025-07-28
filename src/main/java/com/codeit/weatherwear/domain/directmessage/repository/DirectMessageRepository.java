package com.codeit.weatherwear.domain.directmessage.repository;

import com.codeit.weatherwear.domain.directmessage.DirectMessage;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DirectMessageRepository
    extends JpaRepository<DirectMessage, UUID>, DirectMessageCustomRepository {

}
