package eci.ieti.FinzenGoalService.model;

import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "goals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Goal {

    @Id
    private String name;

    @Column(nullable = false)
    private int targetAmount;

    @Column(nullable = false)
    private LocalDate deadline;
}
