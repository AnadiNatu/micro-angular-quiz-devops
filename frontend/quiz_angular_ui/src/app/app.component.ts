import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NgStyle } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NgStyle],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {

  title = 'angular-frontend-quiz_application';

  backgroundImages: string[] = [
    'assets/images/quiz_background_1.jpg',
    'assets/images/quiz_background_2.jpg',
    'assets/images/quiz_background_3.jpg',
    'assets/images/quiz_background_4.jpg'
  ];

  currentBackground: string = this.backgroundImages[0];
  private index = 0;

  ngOnInit(): void {
    setInterval(() => {
      this.index = (this.index + 1) % this.backgroundImages.length;
      this.currentBackground = this.backgroundImages[this.index];
    }, 4000);
  }
}