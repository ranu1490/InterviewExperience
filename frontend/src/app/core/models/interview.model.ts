export type ExperienceLevel = 'FRESHER' | 'JUNIOR' | 'MID' | 'SENIOR' | 'STAFF' | 'PRINCIPAL';
export type SelectionStatus = 'SELECTED' | 'REJECTED' | 'OFFER_REJECTED';
export type DifficultyLabel = 'EASY' | 'MEDIUM' | 'HARD';
export type QuestionCategory =
  | 'CODING' | 'DSA' | 'JAVA' | 'SPRING_BOOT' | 'SQL' | 'KAFKA'
  | 'MICROSERVICES' | 'LLD' | 'HLD' | 'HR' | 'BEHAVIORAL';

export const EXPERIENCE_LEVELS: ExperienceLevel[] =
  ['FRESHER', 'JUNIOR', 'MID', 'SENIOR', 'STAFF', 'PRINCIPAL'];
export const SELECTION_STATUSES: SelectionStatus[] = ['SELECTED', 'REJECTED', 'OFFER_REJECTED'];
export const DIFFICULTY_LABELS: DifficultyLabel[] = ['EASY', 'MEDIUM', 'HARD'];
export const QUESTION_CATEGORIES: QuestionCategory[] =
  ['CODING', 'DSA', 'JAVA', 'SPRING_BOOT', 'SQL', 'KAFKA',
    'MICROSERVICES', 'LLD', 'HLD', 'HR', 'BEHAVIORAL'];

export interface RoundDetail {
  roundNumber: number;
  name: string;
  description?: string;
}

export interface QuestionItem {
  category: QuestionCategory;
  question: string;
}

export interface InterviewSummary {
  id: number;
  companyName: string;
  companyLogo?: string;
  jobRole: string;
  experienceLevel: ExperienceLevel;
  interviewDate?: string;
  location?: string;
  selectionStatus: SelectionStatus;
  difficultyScore?: number;
  difficultyLabel?: DifficultyLabel;
  tags: string[];
  authorUsername: string;
  totalLikes: number;
  totalComments: number;
  views: number;
  createdAt: string;
}

export interface Interview extends InterviewSummary {
  yearsOfExperience?: number;
  ctcOffered?: string;
  numberOfRounds?: number;
  rounds: RoundDetail[];
  questions: QuestionItem[];
  overallExperience?: string;
  preparationTips?: string;
  resourcesUsed: string[];
  aiSummary?: string;
  aiSuggestedTopics: string[];
  authorId: number;
  likedByCurrentUser: boolean;
  updatedAt: string;
}

export interface InterviewRequest {
  companyName: string;
  companyLogo?: string;
  jobRole: string;
  experienceLevel: ExperienceLevel;
  yearsOfExperience?: number;
  interviewDate?: string;
  location?: string;
  ctcOffered?: string;
  numberOfRounds?: number;
  rounds: RoundDetail[];
  questions: QuestionItem[];
  overallExperience?: string;
  preparationTips?: string;
  resourcesUsed: string[];
  selectionStatus: SelectionStatus;
  tags: string[];
}

export interface Comment {
  id: number;
  interviewId: number;
  userId: number;
  username: string;
  content: string;
  createdAt: string;
}

export interface Report {
  id: number;
  interviewId: number;
  reporterUserId: number;
  reason: string;
  status: 'PENDING' | 'REVIEWED' | 'DISMISSED';
  createdAt: string;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface SearchCriteria {
  keyword?: string;
  company?: string;
  role?: string;
  experienceLevel?: ExperienceLevel;
  minYearsOfExperience?: number;
  maxYearsOfExperience?: number;
  difficultyLabel?: DifficultyLabel;
  selectionStatus?: SelectionStatus;
  location?: string;
  tag?: string;
  dateFrom?: string;
  dateTo?: string;
  page?: number;
  size?: number;
  sort?: string;
}
