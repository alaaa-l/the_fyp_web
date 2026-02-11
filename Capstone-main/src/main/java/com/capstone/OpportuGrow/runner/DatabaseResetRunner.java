package com.capstone.OpportuGrow.runner;

import com.capstone.OpportuGrow.Repository.ProjectRepository;
import com.capstone.OpportuGrow.Repository.TransactionRepository;
import com.capstone.OpportuGrow.model.Project;
import com.capstone.OpportuGrow.model.ProjectStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Component
public class DatabaseResetRunner implements CommandLineRunner {

    private final TransactionRepository transactionRepository;
    private final ProjectRepository projectRepository;

    @Value("${app.reset-database:false}")
    private boolean resetDatabase;

    public DatabaseResetRunner(TransactionRepository transactionRepository, ProjectRepository projectRepository) {
        this.transactionRepository = transactionRepository;
        this.projectRepository = projectRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (resetDatabase) {
            System.out.println("WARNING: Resetting database transactions and project funding...");

            // 1. Delete all transactions
            transactionRepository.deleteAll();
            System.out.println("All transactions deleted.");

            // 2. Reset all projects
            List<Project> projects = projectRepository.findAll();
            for (Project project : projects) {
                project.setRaisedAmount(0.0);
                // If the project was COMPLETED, reset it to APPROVED.
                if (project.getStatus() == ProjectStatus.COMPLETED) {
                    project.setStatus(ProjectStatus.APPROVED);
                }
            }
            projectRepository.saveAll(projects);
            System.out.println("All projects reset: raisedAmount = 0, status COMPLETED -> APPROVED.");
        }
    }
}
