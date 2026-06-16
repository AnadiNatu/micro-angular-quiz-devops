import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, CommonModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {

  features = [
    { icon: '🎯', title: 'Take Quizzes',   desc: 'Attempt quizzes across multiple categories and difficulty levels.' },
    { icon: '✏️', title: 'Create Quizzes', desc: 'Curators can build custom quizzes and track participant results.' },
    { icon: '📊', title: 'Track Progress', desc: 'View detailed results, scores, and printable report cards.' },
    { icon: '📧', title: 'Email Alerts',   desc: 'Get notified instantly when quizzes are started or completed.' }
  ];

  stats = [
    { value: '3',    label: 'User Roles'  },
    { value: '∞',    label: 'Categories'  },
    { value: '100%', label: 'Free to Use' }
  ];

  currentStat = 0;

  ngOnInit(): void {
    setInterval(() => {
      this.currentStat = (this.currentStat + 1) % this.stats.length;
    }, 2500);
  }
}