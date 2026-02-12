package com.capstone.OpportuGrow.Repository;

import com.capstone.OpportuGrow.model.Proposal;
import com.capstone.OpportuGrow.model.ProposalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProposalRepository extends JpaRepository<Proposal, Long> {
    List<Proposal> findByUserId(int userId);

    List<Proposal> findByStatus(ProposalStatus status);
}
